package com.diegoviana.potato.data.models

data class SensorData (
    val timestamp: Long,
    val heartRate: Float,
    val hrv: Float,
    val eda: Float,
    val skinTemp: Float,
    val movement: Movement
)

data class Movement(
    val x: Float,
    val y: Float,
    val z: Float
)