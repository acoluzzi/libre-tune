package com.colux.libretune.data.local.join

import androidx.room.Entity

@Entity(tableName = "playlist_artist_cross_ref", primaryKeys = ["playlistId", "artistId"])
data class PlaylistArtistCrossRef(
    val playlistId: String,
    val artistId: String
)