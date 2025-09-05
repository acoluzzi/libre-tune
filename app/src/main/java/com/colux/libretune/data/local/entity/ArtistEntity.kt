package com.colux.libretune.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey val artistId: String,
    val name: String,
    val description: String? = null,
    val images: List<ImageAttribute>,
    val discographyId: String? = null,
    val albumsParams: String? = null,
    val singlesEpsParams: String? = null,
    val updateTimestamp: Long? = null
)