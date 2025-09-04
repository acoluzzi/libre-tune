package com.colux.libretune.data.local.join

import androidx.room.Entity


@Entity(
    tableName = "playlist_related_cross_ref",
    primaryKeys = ["parentPlaylistId", "relatedPlaylistId"]
)
data class PlaylistRelatedCrossRef(
    val parentPlaylistId: String,
    val relatedPlaylistId: String
)