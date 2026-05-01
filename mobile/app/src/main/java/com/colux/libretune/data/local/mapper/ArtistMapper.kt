package com.colux.libretune.data.local.mapper

import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.model.Artist


fun ArtistEntity.toDataModel(): Artist {
    return Artist(
        id = this.artistId,
        name = this.name,
        images = this.images.map { it.toDataModel() }
    )
}

fun Artist.toEntity(): ArtistEntity {
    return ArtistEntity(
        artistId = this.id,
        name = this.name,
        images = this.images.map { it.toEntity() },
        description = null,
    )
}

