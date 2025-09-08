package com.colux.libretune.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val songId: String,
    val playedAtTimestamp: Long = System.currentTimeMillis()
)