package com.colux.libretune.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [Index("albumId")],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["playlistId"],
            childColumns = ["albumId"],
            onDelete = ForeignKey.CASCADE // If an album is deleted, its songs are deleted too
        )
    ]
)
data class SongEntity(
    @PrimaryKey val songId: String,
    val title: String,
    val albumId: String?,
    val images: List<ImageAttribute>,
    val views: Long,
    val durationSec: Long? = null,
    val updateTimestamp: Long
)