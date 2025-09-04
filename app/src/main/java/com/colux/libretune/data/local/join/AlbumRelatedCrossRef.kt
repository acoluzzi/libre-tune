package com.colux.libretune.data.local.join

import androidx.room.Entity


@Entity(
    tableName = "album_related_cross_ref",
    primaryKeys = ["parentAlbumId", "relatedAlbumId"]
)
data class AlbumRelatedCrossRef(
    val parentAlbumId: String,
    val relatedAlbumId: String
)