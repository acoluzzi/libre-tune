package com.colux.libretune.data.model.wrapper

import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.PlaylistEntity

data class AlbumWithArtists(
    val albumEntity: PlaylistEntity,
    val artists: List<ArtistEntity>
)