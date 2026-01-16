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
