package com.diegoviana.potato.data.sensors

import android.content.Context
import android.util.Log
import com.diegoviana.potato.data.models.SensorData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt
import kotlin.random.Random
import com.diegoviana.potato.utils.standardDeviation


enum class SimulationState (
    val targetMeanIBI: Float,
    val ibiVariationRange: Float,
    val targetEda: Float
) {
    CALM(targetMeanIBI = 950f, ibiVariationRange = 60f, targetEda = 1.5f), // ~63 bpm
    MILD_STRESS(targetMeanIBI = 750f, ibiVariationRange = 40f, targetEda = 5.0f), // ~80 bpm
    HIGH_STRESS(targetMeanIBI = 550f, ibiVariationRange = 25f, targetEda = 10.0f) //  ~109 bpm
}

class SensorManager(private val context: Context) {



    private val _sensorData = MutableStateFlow<SensorData?>(null)
    val sensorData: StateFlow<SensorData?> = _sensorData

    private var simulationJob: Job? = null
    private var stateTransitionJob: Job? = null
    private val simulationScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var currentState: SimulationState = SimulationState.CALM


    private var currentIBI = SimulationState.CALM.targetMeanIBI
    private val recentIBIs = mutableListOf<Float>()
    private val ibiWindowMillis = 30000L
    private var calculatedSdnn = 0f
    private var lastEda = SimulationState.CALM.targetEda
    private var lastSkinTemp = 36.5f
    private var lastMovementX = 0f
    private var lastMovementY = 0f
    private var lastMovementZ = 0f

    fun startMonitoring() {
        Log.d("SensorManager", "Start Monitoring")
        startSimulating()
    }

    fun stopMonitoring() {
        Log.d("SensorManager", "Stopping monitoring")
        stopSimulation()
    }

    private fun startSimulating() {

        if (simulationJob?.isActive == true) {
            Log.d("SensorManager", "Simulation already active.")
            return
        }
        Log.d("SensorManager", "Starting simulation coroutines...")


        currentState = SimulationState.CALM
        currentIBI = currentState.targetMeanIBI
        recentIBIs.clear()
        calculatedSdnn = 0f
        lastEda = currentState.targetEda
        lastSkinTemp = 36.5f
        lastMovementX = 0f
        lastMovementY = 0f
        lastMovementZ = 0f
        Log.d("SensorManager", "Initial simulation state reset to $currentState.")


        simulationJob = simulationScope.launch {
            Log.d("SensorManager", "Data simulation loop started.")
            while (isActive) {
                try {
                    simulateNextBeatAndCalculateMetric()
                    val delayTime = currentIBI.toLong().coerceIn(100, 2000)
                    delay(delayTime)
                } catch (e: CancellationException) {
                    Log.i("SensorManager", "Data simulation loop cancelled.")
                    break
                } catch (e: Exception) {
                    Log.e("SensorManager", "Error in data simulation loop", e)
                    delay(1000)
                }
            }
            Log.d("SensorManager", "Data simulation loop finished.")
        }

        stateTransitionJob = simulationScope.launch {
            Log.d("SensorManager", "State transition manager started.")
            while (isActive) {
                try {

                    val stateDuration = Random.nextLong(30000, 90001)
                    Log.d("SensorManager", "Current state: $currentState. Next transition in ${stateDuration / 1000}s.")
                    delay(stateDuration)

                    val previousState = currentState
                    currentState = SimulationState.values().filter { it != previousState }.random()
                    Log.i("SensorManager", "--- State Transition: $previousState -> $currentState ---")

                } catch (e: CancellationException) {
                    Log.i("SensorManager", "State transition manager cancelled.")
                    break
                } catch (e: Exception) {
                    Log.e("SensorManager", "Error in state transition manager", e)
                    delay(10000)
                }
            }
            Log.d("SensorManager", "State transition manager finished.")
        }

        Log.d("SensorManager", "Simulation coroutines launched.")
    }

    // A função stopSimulation() estava fora da classe ou com aninhamento incorreto
    private fun stopSimulation() {
        var didCancel = false
        if (simulationJob?.isActive == true) {
            Log.d("SensorManager", "Stopping data simulation job")
            simulationJob?.cancel()
            didCancel = true
        }
        if (stateTransitionJob?.isActive == true) {
            Log.d("SensorManager", "Stopping state transition job")
            stateTransitionJob?.cancel()
            didCancel = true
        }

        if (!didCancel) {
            Log.d("SensorManager", "Simulation jobs already stopped or null")
        }
        simulationJob = null
        stateTransitionJob = null
        Log.d("SensorManager", "Simulation stopped")
    }


    private fun simulateNextBeatAndCalculateMetric() {
        val targetMeanIBI = currentState.targetMeanIBI
        val ibiVariationRange = currentState.ibiVariationRange
        val randomVariation = Random.nextDouble(-ibiVariationRange.toDouble(), ibiVariationRange.toDouble()).toFloat()
        val smoothingFactor = 0.1f
        currentIBI += (targetMeanIBI - currentIBI) * smoothingFactor + randomVariation
        currentIBI = currentIBI.coerceIn(300f, 1800f)//  Limits IBI - 33bpm to 200bpm

        // Add IBI to the recent list and keep the window
        recentIBIs.add(currentIBI)
        val safeCurrentIBI = currentIBI.coerceAtLeast(100f) // Use a safe minimum IBI value for the calculation
        val estimatedSamplesInWindow = (ibiWindowMillis / safeCurrentIBI).roundToInt() + 5
        while (recentIBIs.size > estimatedSamplesInWindow && recentIBIs.size > 10) {
            recentIBIs.removeAt(0)
        }

        // Calculate HR from the average of recent IBIs
        val averageIBI = if (recentIBIs.isNotEmpty()) recentIBIs.average().toFloat() else currentIBI
        val calculatedHR = if (averageIBI > 0) 60000f / averageIBI else 0f

        //  Calculate SDNN (Standard Deviation of Recent IBIs)
        calculateAndSetSdnn()
        simulateEdaAndTemp()
        simulateMovement()

        val newData = SensorData(
            heartRate = calculatedHR,
            hrv = calculatedSdnn,
            eda = lastEda,
            skinTemp = lastSkinTemp,
            movementX = lastMovementX,
            movementY = lastMovementY,
            movementZ = lastMovementZ,
            timestamp = System.currentTimeMillis()
        )
        _sensorData.value = newData
        Log.v("SensorManager",
            "New data:  " +
                    "State=$currentState, " +
                    "HR=${newData.heartRate.roundToInt()}, " +
                    "SDNN=${String.format("%.1f", newData.hrv)}, " +
                    "EDA=${String.format("%.1f", newData.eda)}")
    }


    private fun calculateAndSetSdnn() {
        calculatedSdnn = recentIBIs.standardDeviation()
    }

    private fun simulateEdaAndTemp() {
        val targetEda = currentState.targetEda
        val edaSmoothingFactor = 0.05f
        val edaNoise = Random.nextDouble(-0.2, 0.2).toFloat()

        lastEda += (targetEda - lastEda) * edaSmoothingFactor + edaNoise
        lastEda = lastEda.coerceIn(0.2f, 20f)

        lastSkinTemp += Random.nextDouble(-0.05, 0.05).toFloat()
        lastSkinTemp = lastSkinTemp.coerceIn(35.0f, 38.5f)
    }

    private fun simulateMovement() {
        lastMovementX += Random.nextDouble(-0.25, 0.25).toFloat()
        lastMovementY += Random.nextDouble(-0.25, 0.25).toFloat()
        lastMovementZ += Random.nextDouble(-0.25, 0.25).toFloat()
    }
}