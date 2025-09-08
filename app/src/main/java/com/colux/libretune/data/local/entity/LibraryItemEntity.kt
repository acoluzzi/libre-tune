package com.colux.libretune.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class LibraryItemType { ARTIST, PLAYLIST }

@Entity(
    tableName = "library",
    indices = [Index("playlistId"), Index("artistId")],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["playlistId"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ArtistEntity::class,
            parentColumns = ["artistId"],
            childColumns = ["artistId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LibraryEntity(
    @PrimaryKey val id: String,
    val type: LibraryItemType,
    val playlistId: String? = null,
    val artistId: String? = null,
    val addedAtTimestamp: Long
)