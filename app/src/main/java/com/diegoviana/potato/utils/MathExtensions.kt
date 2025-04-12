package com.diegoviana.potato.utils

import kotlin.math.sqrt

fun List<Float>.standardDeviation(): Float {
    if (this.size < 2) return 0f
    val mean = this.average().toFloat()
    val sumOfSquareDifferences = this.sumOf { ((it - mean) * (it - mean)).toDouble() }.toFloat()
    val variance = sumOfSquareDifferences / this.size
    return sqrt(variance)
}