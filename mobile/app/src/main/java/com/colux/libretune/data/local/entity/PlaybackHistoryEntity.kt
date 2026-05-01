package com.colux.libretune.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val historyId: Long = 0,
    val songId: String,
    val albumId: String? = null,
    val playedAtTimestamp: Long = System.currentTimeMillis()
)