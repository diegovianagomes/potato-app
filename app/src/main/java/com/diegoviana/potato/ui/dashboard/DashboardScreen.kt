package com.diegoviana.potato.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.diegoviana.potato.data.models.SensorData
import com.diegoviana.potato.data.repository.AlertRepository
import com.diegoviana.potato.data.repository.SensorRepository
import com.diegoviana.potato.ui.alerts.AlertsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    sensorRepository: SensorRepository,
    alertRepository: AlertRepository,
) {
    val sensorDataHistory by sensorRepository.sensorDataHistory.collectAsState() //TODO
    val alerts by alertRepository.alerts.collectAsState() //TODO
    val latestData = sensorDataHistory.lastOrNull()

    var isSimulating by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Título
        Text(
            text = "Potato Project",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Botão para iniciar/parar simulação
        Button(
            onClick = {
                isSimulating = !isSimulating //TODO
                if (isSimulating) {
                    sensorRepository.startMonitoring()
                    alertRepository.startMonitoring()
                } else {
                    sensorRepository.stopMonitoring()
                    alertRepository.stopMonitoring()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSimulating) Color.Red else Color.Green
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSimulating) "Parar Simulação" else "Iniciar Simulação")
        }

        // Cartões de dados dos sensores
        SensorDataCards(latestData)

        // Tabs para gráficos e alertas
        var selectedTab by remember { mutableStateOf(0) }
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Dados Monitorados") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row {
                        Text("Alertas")
                        if (alerts.isNotEmpty()) {
                            Badge { Text(alerts.size.toString()) }
                        }
                    }
                }
            )
        }

        when (selectedTab) {
            0 -> SensorDataChart(sensorDataHistory)
            1 -> AlertsScreen(alerts)
        }
    }
}

@Composable
fun SensorDataCards(sensorData: SensorData?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SensorCard(
            title = "FC",
            description = "Frequência Cardíaca",
            value = sensorData?.heartRate?.toInt()?.toString() ?: "--",
            unit = "BPM"
        )

        SensorCard(
            title = "VFC",
            description = "Variabilidade FC",
            value = sensorData?.hrv?.toInt()?.toString() ?: "--",
            unit = "ms"
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SensorCard(
            title = "EDA",
            description = "Atividade Eletrodérmica",
            value = sensorData?.eda?.toString()?.take(4) ?: "--",
            unit = "μS"
        )
        SensorCard(
            title = "Temp",
            description = "Temperatura da Pele",
            value = sensorData?.skinTemp?.toString()?.take(4) ?: "--",
            unit = "°C"
        )
    }
}

@Composable
fun SensorCard(title: String, description: String, value: String, unit: String) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .height(100.dp)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$value $unit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SensorDataChart(sensorDataHistory: List<SensorData>) {
    // Implementação do gráfico de dados dos sensores
    // TODO
    Text("Gráfico será implementado aqui")
}