package com.colux.libretune.ui.util

import android.annotation.SuppressLint

class TimeUtils {
    companion object {
        @SuppressLint("DefaultLocale")
        fun formatDuration(millis: Long): String {
            val minutes = (millis / 1000) / 60
            val seconds = (millis / 1000) % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    }

}