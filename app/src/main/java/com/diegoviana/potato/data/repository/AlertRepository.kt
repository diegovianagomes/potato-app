package com.diegoviana.potato.data.repository

import com.diegoviana.potato.data.models.Alert
import com.diegoviana.potato.data.models.SensorData
import com.diegoviana.potato.data.models.Severity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class AlertRepository(private val sensorRepository: SensorRepository) {
    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts

    private var stressLevel = 0f
    private var stressIncreasing = false
    private var alertJob: Job? = null

    fun startMonitoring() {
        alertJob = CoroutineScope(Dispatchers.Default).launch {
            sensorRepository.sensorDataHistory.collect { sensorDataList ->
                if (sensorDataList.isNotEmpty()) {
                    analyzeDataForAlerts(sensorDataList.last())
                }
            }
        }
        scheduleStressEvent()
    }

    fun stopMonitoring() {
        alertJob?.cancel()
        alertJob = null
    }

    private fun analyzeDataForAlerts(sensorData: SensorData) {
        // Lógica simplificada para detectar padrões que podem gerar alertas
        if (stressIncreasing) {
            stressLevel += 0.1f
        } else {
            stressLevel = (stressLevel - 0.05f).coerceAtLeast(0f)
        }

        // Gerar alerta quando o nível de estresse ultrapassa um limiar
        if (stressLevel > 0.8f) {
            generateAlert(sensorData)
            stressLevel = 0f
        }
    }

    private fun generateAlert(sensorData: SensorData) {
        val alertTypes = listOf(
            Triple("Possível Ansiedade Detectada",
                "Detectamos uma frequência cardíaca elevada e mudanças na condutância da pele, o que pode ser um sinal de ansiedade. Fique tranquilo, estamos aqui para ajudar!",
                Severity.MEDIUM),
            Triple("Padrão de Estresse Elevado",
                "Seus indicadores apontam para uma resposta intensa ao estresse. Estamos aqui para ajudar você a entender melhor o que pode estar acontecendo.",
                Severity.HIGH),
            Triple("Padrão de Movimento Repetitivo",
                "Detectamos movimentos repetitivos. Isso pode ser uma forma natural de autorregulação e conforto. Tudo bem se você precisar desse momento!",
                Severity.LOW)
        )

        val selectedAlert = alertTypes.random()

        val alert = Alert(
            id = "alert-${System.currentTimeMillis()}",
            timestamp = System.currentTimeMillis(),
            title = selectedAlert.first,
            description = selectedAlert.second,
            severity = selectedAlert.third,
            sensorData = sensorData
        )

        _alerts.update { it + alert }
    }

    private fun scheduleStressEvent() {
        CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                // Programar um evento de estresse após um intervalo aleatório
                val randomTime = (30000 + Math.random() * 60000).toLong() // Entre 30s e 90s
                delay(randomTime)

                stressIncreasing = true

                // O evento de estresse dura de 15 a 30 segundos
                val stressDuration = (15000 + Math.random() * 15000).toLong()
                delay(stressDuration)

                stressIncreasing = false
            }
        }
    }
}