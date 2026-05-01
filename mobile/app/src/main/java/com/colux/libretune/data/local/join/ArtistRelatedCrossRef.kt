package com.colux.libretune.data.local.join

import androidx.room.Entity

@Entity(tableName = "artist_related_cross_ref", primaryKeys = ["parentArtistId", "relatedArtistId"])
data class ArtistRelatedCrossRef(
    val parentArtistId: String,
    val relatedArtistId: String
)