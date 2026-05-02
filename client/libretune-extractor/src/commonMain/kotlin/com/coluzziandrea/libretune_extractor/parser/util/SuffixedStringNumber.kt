package com.coluzziandrea.libretune_extractor.parser.util

/**
 * Converts a string with a number suffix (K, M, B) to a Long.
 * This function is case-insensitive and handles decimals.
 * Examples: "384M" -> 384_000_000, "1.5K" -> 1_500, "500" -> 500
 *
 * @return The parsed Long, or null if the string is not a valid number.
 */
fun String.toSuffixedLong(): Long? {
    val trimmed = this.trim()
    if (trimmed.isEmpty()) return null

    val lastChar = trimmed.last().uppercaseChar()

    val multiplier = when (lastChar) {
        'K' -> 1_000L
        'M' -> 1_000_000L
        'B' -> 1_000_000_000L
        else -> 1L // Default multiplier is 1
    }

    val numberString = if (multiplier != 1L) {
        trimmed.dropLast(1)
    } else {
        trimmed
    }

    // Use toDoubleOrNull to handle potential decimals like "1.5K"
    return numberString.toDoubleOrNull()?.let {
        (it * multiplier).toLong()
    }
}