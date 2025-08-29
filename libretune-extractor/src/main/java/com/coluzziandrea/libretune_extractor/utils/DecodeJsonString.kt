package com.coluzziandrea.libretune_extractor.utils

fun decodeJsonLikeString(input: String): String {
    // Fast path: If there are no backslashes, no decoding is needed.
    // This avoids all allocations and looping for the most common case.
    val firstSlash = input.indexOf('\\')
    if (firstSlash == -1) {
        return input
    }

    val result = StringBuilder(input.length)
    var lastIndex = 0

    // Use indexOf to find escape characters instead of checking every character.
    var nextSlash = firstSlash
    while (nextSlash != -1) {
        // Append the large chunk of text before the found backslash.
        result.append(input, lastIndex, nextSlash)

        // Ensure there's a character to escape after the backslash.
        if (nextSlash + 1 >= input.length) {
            result.append('\\') // Dangling backslash at the end.
            lastIndex = nextSlash + 1
            break
        }

        when (val escapedChar = input[nextSlash + 1]) {
            '\\', '"' -> {
                result.append(escapedChar)
                lastIndex = nextSlash + 2
            }

            'x' -> {
                // Check if there are enough characters for a hex code (\xHH).
                if (nextSlash + 3 < input.length) {
                    val d1 = hexCharToInt(input[nextSlash + 2])
                    val d2 = hexCharToInt(input[nextSlash + 3])

                    if (d1 != -1 && d2 != -1) {
                        // Valid hex code found, convert and append.
                        val hexCode = (d1 shl 4) or d2
                        result.append(hexCode.toChar())
                        lastIndex = nextSlash + 4
                    } else {
                        // Invalid hex, so append the backslash literally.
                        result.append('\\')
                        lastIndex = nextSlash + 1
                    }
                } else {
                    // Incomplete \x sequence, append backslash literally.
                    result.append('\\')
                    lastIndex = nextSlash + 1
                }
            }

            else -> {
                // Unrecognized escape sequence, append the backslash literally.
                result.append('\\')
                lastIndex = nextSlash + 1
            }
        }
        // Find the next backslash to continue the process.
        nextSlash = input.indexOf('\\', lastIndex)
    }

    // Append any remaining part of the string after the last escape sequence.
    if (lastIndex < input.length) {
        result.append(input, lastIndex, input.length)
    }

    return result.toString()
}

// A fast, allocation-free helper to convert a hex char to its integer value.
private fun hexCharToInt(c: Char): Int {
    return when (c) {
        in '0'..'9' -> c - '0'
        in 'a'..'f' -> c - 'a' + 10
        in 'A'..'F' -> c - 'A' + 10
        else -> -1 // Indicates an invalid hex character
    }
}