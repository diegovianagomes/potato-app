package com.diegoviana.potato.data.models

import java.sql.Timestamp

data class Alert (
    val id: String,
    val timestamp: Long,
    val title: String,
    val description: String,
    val severity: Severity,
    val sensorData: SensorData
)

enum class Severity {
    LOW,
    MEDIUM,
    HIGH
}