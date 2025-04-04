package com.diegoviana.potato

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.diegoviana.potato.data.repository.AlertRepository
import com.diegoviana.potato.data.repository.SensorRepository
import com.diegoviana.potato.data.sensors.SensorManager
import com.diegoviana.potato.ui.dashboard.DashboardScreen
import com.diegoviana.potato.ui.theme.PotatoTheme

class MainActivity : ComponentActivity() {

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorRepository: SensorRepository
    private lateinit var alertRepository: AlertRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = SensorManager(this)
        sensorRepository = SensorRepository(sensorManager)
        alertRepository = AlertRepository(sensorRepository)

        setContent {
            PotatoTheme {
                DashboardScreen(
                    sensorRepository = sensorRepository,
                    alertRepository = alertRepository,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorRepository.startMonitoring()
        alertRepository.startMonitoring()
    }

    override fun onPause() {
        super.onPause()
        sensorRepository.stopMonitoring()
        alertRepository.stopMonitoring()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PotatoTheme {
        Greeting("Android")
    }
}