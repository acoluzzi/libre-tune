package com.colux.libretune.data.local.join

import androidx.room.Entity

@Entity(tableName = "artist_artist_cross_ref", primaryKeys = ["parentArtistId", "relatedArtistId"])
data class ArtistArtistCrossRef(
    val parentArtistId: String,
    val relatedArtistId: String
)