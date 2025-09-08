package com.colux.libretune.data.model.wrapper

import com.colux.libretune.data.local.entity.AlbumEntity
import com.colux.libretune.data.local.entity.ArtistEntity

data class AlbumWithArtists(
    val albumEntity: AlbumEntity,
    val artists: List<ArtistEntity>
)