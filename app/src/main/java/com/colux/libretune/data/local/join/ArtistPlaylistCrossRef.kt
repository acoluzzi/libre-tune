package com.colux.libretune.data.local.join

import androidx.room.Entity


@Entity(tableName = "artist_playlists_cross_ref", primaryKeys = ["playlistId", "artistId"])
data class ArtistPlaylistCrossRef(
    val playlistId: String,
    val artistId: String
)