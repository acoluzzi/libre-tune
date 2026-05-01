package com.colux.libretune.ui.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import kotlin.time.Duration

/**
 * A smart throttling operator that can compare the previous and current values.
 *
 * @param period The throttling time window.
 * @param emitNowPredicate A function that takes the previous (nullable) and current item
 * and returns true if the current item should be emitted instantly.
 */
fun <T> Flow<T>.smartThrottle(
    period: Duration,
    emitNowPredicate: (previous: T?, current: T) -> Boolean = { _, _ -> false }
): Flow<T> {
    var lastEmitTime = 0L
    var previousItem: T? = null

    return this.transform { currentItem ->
        val currentTime = System.currentTimeMillis()

        val shouldEmit =
            emitNowPredicate(previousItem, currentItem) || // Emit if the predicate is true
                    (currentTime - lastEmitTime) >= period.inWholeMilliseconds // Emit if time is up

        if (shouldEmit) {
            emit(currentItem)
            lastEmitTime = currentTime
            previousItem = currentItem
        }
    }
}