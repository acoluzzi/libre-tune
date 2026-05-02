package com.colux.libretune.data.local.join

import androidx.room.Entity


@Entity(tableName = "history_artist_cross_ref", primaryKeys = ["historyId", "artistId"])
data class HistoryArtistCrossRef(
    val historyId: Long,
    val artistId: String
)