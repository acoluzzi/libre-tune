package com.colux.libretune.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AlbumType { ALBUM, SINGLE_EP }

@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val albumId: String,
    val name: String,
    val images: List<ImageAttribute>,
    val type: AlbumType,
    val releaseYear: Int,
    val updateTimestamp: Long? = null
)