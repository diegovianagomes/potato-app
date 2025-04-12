package com.diegoviana.potato.data.repository

import android.util.Log
import com.diegoviana.potato.data.models.Alert
import com.diegoviana.potato.data.models.SensorData
import com.diegoviana.potato.data.models.Severity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class AlertRepository(private val sensorRepository: SensorRepository) {
    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts

    private var analysisJob: Job? = null
    private val analysisScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val HIGH_HR_THRESHOLD = 95f
    private val LOW_SDNN_THRESHOLD = 30f
    private val HIGH_EDA_THRESHOLD = 7.0f
    private val CONDITION_DURATION_POINTS = 8
    private val ALERT_COOLDOWN_MS = 120 * 1000L


    private var highStressConditionMet = false
    private var lastHighStressAlertTime = 0L

    private enum class AlertType {
        HIGH_STRESS,
        MEDIUM,
        CALM_RECOVERY
    }

    fun startMonitoring() {
        analysisJob?.cancel()
        analysisJob = analysisScope.launch {
            Log.d("AlertRepository", "Starting data analysis for alerts.")
            sensorRepository.sensorDataHistory.collect { sensorDataList ->
                if (sensorDataList.size >= CONDITION_DURATION_POINTS) {
                    analyzeDataForAlerts(sensorDataList)
                }
            }
        }
        Log.d("AlertRepository", "Alert analysis job launched.")
    }

    fun stopMonitoring() {
        if (analysisJob?.isActive == true) {
            Log.d("AlertRepository", "Stopping data analysis for alerts.")
            analysisJob?.cancel()
        }
        analysisJob = null
    }

    private fun analyzeDataForAlerts(sensorDataList: List<SensorData>) {
        val recentData = sensorDataList.takeLast(CONDITION_DURATION_POINTS)

        val isHighStressPattern = recentData.all { data ->
            data.heartRate > HIGH_HR_THRESHOLD &&
                    data.hrv < LOW_SDNN_THRESHOLD &&
                    data.eda > HIGH_EDA_THRESHOLD
        }

        val now = System.currentTimeMillis()

        if (isHighStressPattern && !highStressConditionMet) {
            Log.i("AlertRepository", "High Stress Pattern detected consistently for $CONDITION_DURATION_POINTS points.")
            highStressConditionMet = true

            if (now - lastHighStressAlertTime > ALERT_COOLDOWN_MS) {
                Log.w("AlertRepository", "Generating High Stress Alert!")
                generateAlert(recentData.last(), AlertType.HIGH_STRESS)
                lastHighStressAlertTime = now
            } else {
                Log.d("AlertRepository", "High Stress Pattern detected, but alert is on cooldown.")
            }
        } else if (!isHighStressPattern && highStressConditionMet) {
            Log.d("AlertRepository", "High Stress Pattern no longer detected.")
            highStressConditionMet = false
        }
    }

    private fun generateAlert(sensorData: SensorData, type: AlertType) {
        val title: String
        val description: String
        val severity: Severity

        when (type) {
            AlertType.HIGH_STRESS -> {
                title = "Padrão de Estresse Elevado"
                description = "Detectamos uma combinação de frequência cardíaca alta (${sensorData.heartRate.toInt()} bpm), baixa variabilidade (SDNN ${String.format("%.1f", sensorData.hrv)} ms) e alta atividade eletrodérmica (EDA ${String.format("%.1f", sensorData.eda)} µS) recentemente. Respire fundo, estamos aqui para ajudar."
                severity = Severity.HIGH
            }

            AlertType.MEDIUM -> {
                title = "Frequência Cardíaca Baixa"
                description = "Sua frequência cardíaca esteve consistentemente abaixo do normal (${sensorData.heartRate.toInt()} bpm). Verifique como você está se sentindo."
                severity = Severity.MEDIUM

                Log.w("AlertRepository", "LOW_HR alert generation triggered but not fully implemented.")
                return
            }
            AlertType.CALM_RECOVERY -> {
                title = "Período de Calma Detectado"
                description = "Seus indicadores (FC: ${sensorData.heartRate.toInt()} bpm, SDNN: ${String.format("%.1f", sensorData.hrv)} ms) sugerem um período de relaxamento ou recuperação. Ótimo!"
                severity = Severity.LOW
                Log.w("AlertRepository", "CALM_RECOVERY alert generation triggered but not fully implemented.")
                return
            }
        }

        val alert = Alert(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            title = title,
            description = description,
            severity = severity,
            sensorData = sensorData
        )

        if (_alerts.value.find { it.title == alert.title && (System.currentTimeMillis() - it.timestamp < 10000) } == null) {
            _alerts.update { currentAlerts ->
                (currentAlerts + alert).takeLast(50)
            }
        }
    }
}