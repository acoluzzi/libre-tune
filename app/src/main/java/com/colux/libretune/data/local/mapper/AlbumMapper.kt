package com.colux.libretune.data.local.mapper

import com.colux.libretune.data.local.entity.AlbumEntity
import com.colux.libretune.data.local.entity.AlbumType
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.model.Playlist


fun AlbumEntity.toDataModel(artistEntity: List<ArtistEntity>?): Playlist {
    return Playlist(
        id = this.albumId,
        name = this.name,
        images = this.images.map { it.toDataModel() },
        artists = artistEntity?.map { it.toDataModel() } ?: emptyList()
    )
}


fun Playlist.toSimpleEntity(type: AlbumType): AlbumEntity {
    return AlbumEntity(
        albumId = this.id,
        name = this.name,
        images = this.images.map { it.toEntity() },
        type = type
    )
}
