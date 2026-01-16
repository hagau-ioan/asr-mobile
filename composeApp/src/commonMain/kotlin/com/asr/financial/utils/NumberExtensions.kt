package com.asr.financial.utils

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Format a Double to a string with specified decimal places
 */
fun Double.formatDecimal(decimals: Int = 2): String {
    val multiplier = 10.0.pow(decimals)
    val rounded = (this * multiplier).roundToInt() / multiplier
    return rounded.toString()
}

/**
 * Format a Double as currency (RON)
 */
fun Double.formatCurrency(): String {
    return "${this.formatDecimal(2)} RON"
}

/**
 * Round a Double to specified decimal places
 */
fun Double.roundTo(decimals: Int = 2): Double {
    val multiplier = 10.0.pow(decimals)
    return (this * multiplier).roundToInt() / multiplier
}

/**
 * Calculate percentage
 */
fun Double.percentOf(total: Double): Double {
    if (total == 0.0) return 0.0
    return (this / total) * 100
}

/**
 * Calculate percentage as Int
 */
fun Double.percentOfAsInt(total: Double): Int {
    return percentOf(total).toInt()
}

/**
 * Add two numbers safely
 */
fun Double.add(other: Double): Double = this + other

/**
 * Subtract two numbers safely
 */
fun Double.subtract(other: Double): Double = this - other

/**
 * Multiply two numbers safely
 */
fun Double.multiply(other: Double): Double = this * other

/**
 * Divide two numbers safely (returns 0 if divisor is 0)
 */
fun Double.divide(other: Double): Double {
    if (other == 0.0) return 0.0
    return this / other
}

/**
 * Format number for chart axis labels
 * Values >= 1000 are shown as "XK+" (e.g., 40345 -> 40K+)
 * Values < 1000 are shown as integers (e.g., 500 -> 500)
 */
fun Double.formatForAxis(): String {
    return when {
        this >= 1000 -> "${(this / 1000).toInt()}K+"
        else -> this.toInt().toString()
    }
}

/**
 * Format percentage with sign and 1 decimal place
 * Examples: 15.67 -> "+15.7%", -8.34 -> "-8.3%"
 */
fun Double.formatPercentage(): String {
    val rounded = this.roundTo(1)
    val sign = if (rounded >= 0) "+" else ""
    return "$sign$rounded%"
}
