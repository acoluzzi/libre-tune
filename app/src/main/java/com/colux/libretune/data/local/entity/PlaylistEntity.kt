package com.colux.libretune.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val playlistId: String,
    val name: String,
    val images: List<ImageAttribute>,
    val isLocal: Boolean,
    val updateTimestamp: Long? = null
)