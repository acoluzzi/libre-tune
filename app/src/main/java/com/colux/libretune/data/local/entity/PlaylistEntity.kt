package com.colux.libretune.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AlbumType { ALBUM, SINGLE, EP, PLAYLIST }

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val playlistId: String,
    val name: String,
    val images: List<ImageAttribute>,
    val type: AlbumType,
    val isLocal: Boolean? = false,
    val releaseYear: Int? = null,
    val updateTimestamp: Long? = null,
    /**
     * YouTube Music playlist ID this local playlist mirrors. Null for plain
     * local playlists and for non-synced browsed playlists.
     */
    val remotePlaylistId: String? = null,
    val syncEnabled: Boolean = false
)
