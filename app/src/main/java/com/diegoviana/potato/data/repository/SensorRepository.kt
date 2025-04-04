package com.diegoviana.potato.data.repository

import com.diegoviana.potato.data.models.SensorData
import com.diegoviana.potato.data.sensors.SensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SensorRepository(private val sensorManager: SensorManager) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _sensorDataHistory = MutableStateFlow<List<SensorData>>(emptyList())
    val sensorDataHistory: StateFlow<List<SensorData>> = _sensorDataHistory

    init {
        scope.launch {
            sensorManager.sensorData.collect { newData ->
                newData?.let { data ->
                    val currentList = _sensorDataHistory.value.toMutableList()
                    currentList.add(data)
                    if (currentList.size > 100) currentList.removeAt(0)
                    _sensorDataHistory.value = currentList
                }
            }
        }
    }

    fun startMonitoring() {
        sensorManager.startMonitoring()
    }

    fun stopMonitoring() {
        sensorManager.stopMonitoring()
    }
}