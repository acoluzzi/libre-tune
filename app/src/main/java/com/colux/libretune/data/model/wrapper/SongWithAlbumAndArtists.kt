package com.colux.libretune.data.model.wrapper

import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.PlaylistEntity
import com.colux.libretune.data.local.entity.SongEntity

data class SongWithAlbumAndArtists(
    val songEntity: SongEntity,
    val albumEntity: PlaylistEntity?,
    val artists: List<ArtistEntity>
)