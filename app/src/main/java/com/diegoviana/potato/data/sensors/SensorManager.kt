
package com.diegoviana.potato.data.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import com.diegoviana.potato.data.models.SensorData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SensorManager(private val context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
    private val heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _sensorData = MutableStateFlow<SensorData?>(null)
    val sensorData: StateFlow<SensorData?> = _sensorData

    private var lastHeartRate = 75f
    private var lastHrv = 50f
    private var lastEda = 2.5f
    private var lastSkinTemp = 36.5f
    private var lastMovementX = 0f
    private var lastMovementY = 0f
    private var lastMovementZ = 0f

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_HEART_RATE -> {
                    lastHeartRate = event.values[0]
                    simulateHrvCalculation()
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    lastMovementX = event.values[0]
                    lastMovementY = event.values[1]
                    lastMovementZ = event.values[2]
                }
            }
            simulateEdaAndTemp()
            updateSensorData()
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // Not used
        }
    }

    fun startMonitoring() {
        heartRateSensor?.let {
            sensorManager.registerListener(sensorListener, it, android.hardware.SensorManager.SENSOR_DELAY_NORMAL)
        }
        accelerometer?.let {
            sensorManager.registerListener(sensorListener, it, android.hardware.SensorManager.SENSOR_DELAY_NORMAL)
        }

        if (heartRateSensor == null || accelerometer == null) {
            startSimulating()
        }
    }

    fun stopMonitoring() {
        sensorManager.unregisterListener(sensorListener)
        stopSimulation()
    }

    private var simulationJob: Job? = null

    private fun startSimulating() {
        simulationJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                simulateAllSensorsData()
                updateSensorData()
                delay(1000)
            }
        }
    }

    private fun stopSimulation() {
        simulationJob?.cancel()
        simulationJob = null
    }

    private fun simulateAllSensorsData() {
        lastHeartRate += (Math.random() * 2 - 1).toFloat()
        lastHeartRate = lastHeartRate.coerceIn(60f, 100f)

        simulateHrvCalculation()
        simulateEdaAndTemp()

        lastMovementX += (Math.random() * 2 - 1).toFloat()
        lastMovementY += (Math.random() * 2 - 1).toFloat()
        lastMovementZ += (Math.random() * 2 - 1).toFloat()
    }

    private fun simulateHrvCalculation() {
        lastHrv += (Math.random() * 2 - 1).toFloat()
        lastHrv = lastHrv.coerceIn(20f, 80f)
    }

    private fun simulateEdaAndTemp() {
        lastEda += (Math.random() * 0.2 - 0.1).toFloat()
        lastEda = lastEda.coerceIn(1f, 10f)

        lastSkinTemp += (Math.random() * 0.2 - 0.1).toFloat()
        lastSkinTemp = lastSkinTemp.coerceIn(35f, 38f)
    }

    private fun updateSensorData() {
        _sensorData.value = SensorData(
            heartRate = lastHeartRate,
            hrv = lastHrv,
            eda = lastEda,
            skinTemp = lastSkinTemp,
            movementX = lastMovementX,
            movementY = lastMovementY,
            movementZ = lastMovementZ
        )
    }
}