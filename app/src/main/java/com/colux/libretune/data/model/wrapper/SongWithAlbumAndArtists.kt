package com.colux.libretune.data.model.wrapper

import com.colux.libretune.data.local.entity.AlbumEntity
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.SongEntity

data class SongWithAlbumAndArtists(
    val songEntity: SongEntity,
    val albumEntity: AlbumEntity?,
    val artists: List<ArtistEntity>
)