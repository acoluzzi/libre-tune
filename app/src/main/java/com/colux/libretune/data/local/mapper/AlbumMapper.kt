package com.colux.libretune.data.local.mapper

import com.colux.libretune.data.local.entity.AlbumEntity
import com.colux.libretune.data.local.entity.AlbumType
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.PlaylistEntity
import com.colux.libretune.data.local.wrapper.AlbumWithArtists
import com.colux.libretune.data.model.Playlist
import com.colux.libretune.data.model.PlaylistType


fun AlbumEntity.toDataModel(artistEntity: List<ArtistEntity>?): Playlist {
    return Playlist(
        id = this.albumId,
        name = this.name,
        images = this.images.map { it.toDataModel() },
        artists = artistEntity?.map { it.toDataModel() } ?: emptyList(),
        isLocal = false
    )
}


fun Playlist.toEntity(albumType: AlbumType = AlbumType.ALBUM): AlbumEntity {
    return AlbumEntity(
        albumId = this.id,
        name = this.name,
        images = this.images.map { it.toEntity() },
        releaseYear = this.releaseYear ?: 0,
        type = when (this.type) {
            PlaylistType.ALBUM -> AlbumType.ALBUM
            PlaylistType.SINGLE_EP -> AlbumType.SINGLE_EP
            else -> AlbumType.ALBUM
        }
    )
}

fun Playlist.toPlaylistEntity(): PlaylistEntity {
    return PlaylistEntity(
        playlistId = this.id,
        name = this.name,
        images = this.images.map { it.toEntity() },
        isLocal = false
    )
}

fun PlaylistEntity.toDataModel(): Playlist {
    return Playlist(
        id = this.playlistId,
        name = this.name,
        images = this.images.map { it.toDataModel() },
        artists = emptyList(),
        type = PlaylistType.PLAYLIST,
        isLocal = this.isLocal
    )
}


fun AlbumWithArtists.toDataModel(): Playlist {
    return Playlist(
        id = this.albumEntity.albumId,
        name = this.albumEntity.name,
        releaseYear = this.albumEntity.releaseYear,
        images = this.albumEntity.images.map { it.toDataModel() },
        artists = this.artists.map { it.toDataModel() },
        type = when (this.albumEntity.type) {
            AlbumType.ALBUM -> PlaylistType.ALBUM
            AlbumType.SINGLE_EP -> PlaylistType.SINGLE_EP
        },
        isLocal = false
    )
}