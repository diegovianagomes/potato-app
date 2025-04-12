
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

class SensorManager(private val context: Context) {

    private val _sensorData = MutableStateFlow<SensorData?>(null)
    val sensorData: StateFlow<SensorData?> = _sensorData

    private var simulationJob: Job? = null
    private val simulationScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var currentIBI = 800f // Initial IBI value 75 bpm
    private val recentIBIs = mutableListOf<Float>()
    private val ibiWindowMillis = 30000L // Window with 30 sec. to calculate the average IBI

    //private var lastHrv = 50f
    private var calculatedSdnn = 0f
    private var lastEda = 2.5f
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
            Log.d("SensorManager", "Simulation already running")
            return
        }
        Log.d("SensorManager", "Starting simulation")
        simulationJob = simulationScope.launch {
            // reset initial state
            currentIBI = 800f
            recentIBIs.clear()
            calculatedSdnn = 0f
            lastEda = 2.5f
            lastSkinTemp = 36.5f
            lastMovementX = 0f
            lastMovementY = 0f
            lastMovementZ = 0f
            Log.d("SensorManager", "Initial simulation state reset.")

            while(isActive) {
                try {
                    simulateNextBeatAndCalculateMetric()
                    val delayTime = currentIBI.toLong().coerceIn(100, 2000)
                    delay(delayTime)
                } catch (e: CancellationException) {
                    Log.i("SensorManager", "Simulation cancelled")
                    break
                } catch (e: Exception) {
                    Log.e("SensorManager", "Error in simulation loop", e)
                    delay(1000)
                }
            }
            Log.d("SensorManager", "Simulation loop finished")
        }
        Log.d("SensorManager", "Simulation started")
    }

    private fun stopSimulation() {
        if (simulationJob?.isActive == true) {
            Log.d("SensorManager", "Stopping simulation")
            simulationJob?.cancel()
        } else {
            Log.d("SensorManager", "Simulation already stopped or null")
        }
        simulationJob = null
        Log.d("SensorManager", "Simulation stopped")
    }

    private fun simulateNextBeatAndCalculateMetric() {
        val ibiVariation = Random.nextDouble(-50.0, 50.0).toFloat()
        currentIBI += ibiVariation
        currentIBI = currentIBI.coerceIn(400f, 1500f) //  Limits IBI - 40bpm to 150bpm

        // Add IBI to the recent list and keep the window
        recentIBIs.add(currentIBI)
        val safeCurrentIBI = currentIBI.coerceAtLeast(100f)
        val estimatedSamplesInWindow = (ibiWindowMillis / currentIBI).coerceAtLeast(400f).roundToInt() + 5
        while (recentIBIs.size > estimatedSamplesInWindow && recentIBIs.size > 10) {
            recentIBIs.removeAt(0)
        }

        // Calculate HR from the average of recent IBIs
        val averageIBI = if (recentIBIs.isNotEmpty()) recentIBIs.average().toFloat() else currentIBI
        val calculatedHR = if (averageIBI > 0) 60000f / averageIBI else 0f

        //simulateHrvPlaceholder(averageIBI)
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
                    "HR=${newData.heartRate.roundToInt()}, " +
                    "SDNN=${String.format("%.1f", newData.hrv)}, " +
                    "EDA=${String.format("%.1f", newData.eda)}")
    }
    private fun calculateAndSetSdnn() {
        calculatedSdnn = recentIBIs.standardDeviation()
    }

    private fun simulateEdaAndTemp() {
        lastEda += Random.nextDouble(-0.1, 0.1).toFloat()
        lastEda = lastEda.coerceIn(0.5f, 15f)

        lastSkinTemp += Random.nextDouble(-0.05, 0.05).toFloat()
        lastSkinTemp = lastSkinTemp.coerceIn(35.0f, 38.5f)
    }

    private fun simulateMovement() {
        lastMovementX += Random.nextDouble(-0.25, 0.25).toFloat()
        lastMovementY += Random.nextDouble(-0.25, 0.25).toFloat()
        lastMovementZ += Random.nextDouble(-0.25, 0.25).toFloat()
    }


}