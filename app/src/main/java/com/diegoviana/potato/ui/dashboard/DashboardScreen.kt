package com.diegoviana.potato.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.diegoviana.potato.R
import com.diegoviana.potato.data.repository.AlertRepository
import com.diegoviana.potato.data.repository.SensorRepository
import com.diegoviana.potato.ui.charts.SensorDataChart
import com.diegoviana.potato.ui.theme.*

@Composable
fun DashboardScreen(
    sensorRepository: SensorRepository,
    alertRepository: AlertRepository,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val sensorDataHistory by sensorRepository.sensorDataHistory.collectAsState()
    val currentSensorData = sensorDataHistory.lastOrNull()
    val alerts by alertRepository.alerts.collectAsState()
    var isMonitoringActive by remember { mutableStateOf(true) }

    // Estado para controlar a visibilidade do drawer
    var isDrawerOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (isMonitoringActive) {
            sensorRepository.startMonitoring()
        }
    }

    // Usamos Box como container principal para poder sobrepor o drawer
    Box(modifier = Modifier.fillMaxSize()) {
        // Conteúdo principal
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = GhibliBackground
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Barra superior com título e avatar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Potato Project",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Avatar do usuário
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GhibliGreen)
                            .border(1.dp, GhibliDeepBlue, CircleShape)
                            .clickable { isDrawerOpen = true }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "DV",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = {
                        isMonitoringActive = !isMonitoringActive
                        if (isMonitoringActive) {
                            sensorRepository.startMonitoring()
                        } else {
                            sensorRepository.stopMonitoring()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .graphicsLayer {
                            shadowElevation = 16f  // Y position = 2
                            shape = RoundedCornerShape(12.dp)
                            clip = true
                            spotShadowColor = if (isMonitoringActive) Color(0xFFA10000) else Color(0xFF777935)
                            ambientShadowColor = if (isMonitoringActive) Color(0xFFA10000) else Color(0xFF777935)
                        }
                        .border(
                            width = 2.dp,
                            color = if (isMonitoringActive) Color(0xFFA10000) else Color(0xFF777935),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMonitoringActive) Color(0xFFE7251A) else Color(0xFF989B55),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Text(
                        text = if (isMonitoringActive) "parar" else "iniciar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Grid de sensores - 2x2
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SensorCard(
                            title = "FC",
                            value = "${currentSensorData?.heartRate?.toInt() ?: "--"}",
                            unit = "BPM",
                            modifier = Modifier.weight(1f)
                        )

                        SensorCard(
                            title = "VFC",
                            value = "${currentSensorData?.hrv?.toInt() ?: "--"}",
                            unit = "ms",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SensorCard(
                            title = "EDA",
                            value = String.format("%.2f", currentSensorData?.eda ?: 0f),
                            unit = "μS",
                            modifier = Modifier.weight(1f)
                        )

                        SensorCard(
                            title = "Temp",
                            value = String.format("%.1f", currentSensorData?.skinTemp ?: 0f),
                            unit = "°C",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { /* Navegar para tela de dados */ }) {
                        Text("Dados Monitorados")
                    }

                    TextButton(onClick = { navController.navigate("alerts") },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = GhibliDeepBlue
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Alertas")
                            if (alerts.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .size(16.dp)
                                        .background(GhibliRed, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${alerts.size}",
                                        color = GhibliCream,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Gráfico
                SensorDataChart(sensorDataHistory = sensorDataHistory)
            }
        }

        // Overlay semi-transparente quando o drawer está aberto
        if (isDrawerOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.9f))
                    .zIndex(10f)
                    .clickable { isDrawerOpen = false }
            )
        }

        // Drawer deslizante
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInHorizontally(
                initialOffsetX = { it }, // Começa fora da tela à direita
                animationSpec = tween(300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it }, // Sai para fora da tela à direita
                animationSpec = tween(300)
            ),
            modifier = Modifier
                .fillMaxHeight()
                .width(280.dp)
                .align(Alignment.CenterEnd)
                .zIndex(11f) // Acima do overlay
        ) {
            Card(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GhibliBackground
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Cabeçalho do drawer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Perfil do Usuário",
                            style = MaterialTheme.typography.titleMedium
                        )

                        IconButton(onClick = { isDrawerOpen = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar"
                            )
                        }
                    }

                    // Avatar grande
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(GhibliGreen)
                            .border(2.dp, GhibliDeepBlue, CircleShape)
                            .align(Alignment.CenterHorizontally)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "DV",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Informações do usuário
                    Text(
                        text = "Diego Viana",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Text(
                        text = "diego@example.com",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    // Opções do menu
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MenuOption(
                            icon = Icons.Default.Person,
                            text = "Editar Perfil"
                        )

                        MenuOption(
                            icon = Icons.Default.Settings,
                            text = "Configurações"
                        )

                        MenuOption(
                            icon = Icons.Default.Info,
                            text = "Sobre o App"
                        )

                        MenuOption(
                            icon = Icons.Default.ExitToApp,
                            text = "Sair"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuOption(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = GhibliDeepBlue
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun SensorCard(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    val cardBackground = Color(0xFFF0E9D2)
    val borderColor = Color(0xFFC8B88A)

    val icon: @Composable () -> Unit = {
        when (title) {
            "FC" -> Icon(
                painter = painterResource(id = R.drawable.ic_heart),
                contentDescription = "Frequência Cardíaca",
                tint = Color(0xFF789F5A)
            )
            "VFC" -> Icon(
                painter = painterResource(id = R.drawable.ic_heart_rate),
                contentDescription = "Variabilidade da Frequência Cardíaca",
                tint = Color(0xFF789F5A)
            )
            "EDA" -> Icon(
                painter = painterResource(id = R.drawable.ic_eda),
                contentDescription = "Atividade Eletrodérmica",
                tint = Color(0xFF789F5A)
            )
            "Temp" -> Icon(
                painter = painterResource(id = R.drawable.ic_temperature),
                contentDescription = "Temperatura",
                tint = Color(0xFF789F5A)
            )
            else -> {}
        }
    }

    Surface(
        modifier = modifier.padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        color = cardBackground,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título no topo
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF789F5A), // Verde Ghibli para títulos
                fontWeight = FontWeight.Medium
            )

            // Ícone e valor no centro
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Ícone à esquerda
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .padding(end = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        icon()
                    }

                    // Valor grande no centro
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF789F5A), // Verde Ghibli para valores
                        fontSize = 24.sp
                    )
                }
            }

            // Unidade na parte inferior
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF789F5A), // Verde Ghibli para unidades
                fontWeight = FontWeight.Medium
            )
        }
    }
}