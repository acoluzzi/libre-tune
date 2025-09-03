package com.colux.libretune.data.local.join

import androidx.room.Entity


@Entity(tableName = "artist_featured_playlist_cross_ref", primaryKeys = ["playlistId", "artistId"])
data class ArtistFeaturedPlaylistCrossRef(
    val playlistId: String,
    val artistId: String
)