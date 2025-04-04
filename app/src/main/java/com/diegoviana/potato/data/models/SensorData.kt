package com.diegoviana.potato.data.models

data class SensorData(
    val heartRate: Float,
    val hrv: Float,
    val eda: Float,
    val skinTemp: Float,
    val movementX: Float = 0f,
    val movementY: Float = 0f,
    val movementZ: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

data class Movement(
    val x: Float,
    val y: Float,
    val z: Float
)