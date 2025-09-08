package com.colux.libretune.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AlbumType { ALBUM, SINGLE_EP, PLAYLIST }

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val playlistId: String,
    val name: String,
    val images: List<ImageAttribute>,
    val type: AlbumType,
    val isLocal: Boolean? = false,
    val releaseYear: Int? = null,
    val updateTimestamp: Long? = null
)