package com.colux.libretune.data.local.join

import androidx.room.Entity

@Entity(tableName = "album_artist_cross_ref", primaryKeys = ["albumId", "artistId"])
data class AlbumArtistCrossRef(
    val albumId: String,
    val artistId: String
)