package com.diegoviana.potato.ui.charts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diegoviana.potato.data.models.SensorData

@Composable
fun SensorDataChart(sensorDataHistory: List<SensorData>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (sensorDataHistory.isEmpty())
                "Sem dados disponíveis"
            else
                "Dados coletados: ${sensorDataHistory.size} leituras"
        )
    }
}