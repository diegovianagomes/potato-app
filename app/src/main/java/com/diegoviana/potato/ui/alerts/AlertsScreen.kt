package com.diegoviana.potato.ui.alerts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diegoviana.potato.data.models.Alert
import com.diegoviana.potato.data.models.Severity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlertsScreen(alerts: List<Alert>) {
    if (alerts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nenhum alerta detectado",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    LazyColumn {
        items(alerts) { alert ->
            AlertItem(alert)
        }
    }
}

@Composable
fun AlertItem(alert: Alert) {
    val backgroundColor = when (alert.severity) {
        Severity.HIGH -> Color.Red.copy(alpha = 0.2f)
        Severity.MEDIUM -> Color.Yellow.copy(alpha = 0.2f)
        Severity.LOW -> Color.Green.copy(alpha = 0.2f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = when (alert.severity) {
                        Severity.HIGH -> Color.Red
                        Severity.MEDIUM -> Color.Yellow
                        Severity.LOW -> Color.Green
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(alert.timestamp)),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = alert.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}