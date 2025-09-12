package com.colux.libretune.data.model

data class HistoryItem(
    val id: Long,
    val song: Song?,
    val playedAt: Long
)