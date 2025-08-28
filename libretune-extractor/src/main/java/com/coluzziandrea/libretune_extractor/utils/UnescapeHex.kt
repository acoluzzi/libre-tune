package com.coluzziandrea.libretune_extractor.utils

fun unescapeHex(text: String): String {
    val result = StringBuilder(text.length)
    var i = 0
    while (i < text.length) {
        val char = text[i]
        if (char == '\\' && i + 3 < text.length && text[i + 1] == 'x') {
            val hex = text.substring(i + 2, i + 4)
            try {
                result.append(hex.toInt(16).toChar())
                i += 4 // Move index past the processed "\xHH" sequence
                continue
            } catch (e: NumberFormatException) {
                // Not a valid hex, append literally
            }
        }
        result.append(char)
        i++
    }
    return result.toString()
}