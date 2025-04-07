package com.diegoviana.potato.ui.charts

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.diegoviana.potato.data.models.SensorData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun SensorDataChart(sensorDataHistory: List<SensorData>) {
    if (sensorDataHistory.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Sem dados disponíveis")
        }
    } else {
        // Usar AndroidView para incorporar o MPAndroidChart
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(8.dp),
            factory = { context ->
                // Criar o gráfico
                LineChart(context).apply {
                    description.isEnabled = false
                    setTouchEnabled(true)
                    isDragEnabled = true
                    setScaleEnabled(true)
                    setPinchZoom(true)

                    // Configurar eixo X
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.setDrawGridLines(false)

                    // Desabilitar eixo Y direito
                    axisRight.isEnabled = false

                    // Configurar eixo Y esquerdo
                    axisLeft.setDrawGridLines(true)

                    // Configurar legenda
                    legend.isEnabled = true
                    legend.textSize = 12f

                    // Animação
                    animateX(1000)
                }
            },
            update = { chart ->
                // Criar entradas para cada tipo de sensor
                val heartRateEntries = sensorDataHistory.mapIndexed { index, data ->
                    Entry(index.toFloat(), data.heartRate.toFloat())
                }

                val hrvEntries = sensorDataHistory.mapIndexed { index, data ->
                    Entry(index.toFloat(), data.hrv.toFloat())
                }

                val edaEntries = sensorDataHistory.mapIndexed { index, data ->
                    Entry(index.toFloat(), data.eda.toFloat())
                }

                val tempEntries = sensorDataHistory.mapIndexed { index, data ->
                    Entry(index.toFloat(), data.skinTemp.toFloat())
                }

                // Criar datasets para cada tipo de sensor
                val heartRateDataSet = LineDataSet(heartRateEntries, "FC (BPM)").apply {
                    color = AndroidColor.RED
                    setCircleColor(AndroidColor.RED)
                    lineWidth = 2f
                    circleRadius = 3f
                    setDrawCircleHole(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawValues(false)
                    axisDependency = YAxis.AxisDependency.LEFT
                }

                val hrvDataSet = LineDataSet(hrvEntries, "VFC (ms)").apply {
                    color = AndroidColor.GREEN
                    setCircleColor(AndroidColor.GREEN)
                    lineWidth = 2f
                    circleRadius = 3f
                    setDrawCircleHole(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawValues(false)
                    axisDependency = YAxis.AxisDependency.LEFT
                }

                val edaDataSet = LineDataSet(edaEntries, "EDA (μS)").apply {
                    color = AndroidColor.BLUE
                    setCircleColor(AndroidColor.BLUE)
                    lineWidth = 2f
                    circleRadius = 3f
                    setDrawCircleHole(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawValues(false)
                    axisDependency = YAxis.AxisDependency.LEFT
                }

                val tempDataSet = LineDataSet(tempEntries, "Temp (°C)").apply {
                    color = AndroidColor.MAGENTA
                    setCircleColor(AndroidColor.MAGENTA)
                    lineWidth = 2f
                    circleRadius = 3f
                    setDrawCircleHole(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawValues(false)
                    axisDependency = YAxis.AxisDependency.LEFT
                }

                // Criar LineData com todos os datasets
                val lineData = LineData(heartRateDataSet, hrvDataSet, edaDataSet, tempDataSet)

                // Atribuir dados ao gráfico
                chart.data = lineData

                // Notificar o gráfico que os dados mudaram
                chart.invalidate()
            }
        )
    }
}