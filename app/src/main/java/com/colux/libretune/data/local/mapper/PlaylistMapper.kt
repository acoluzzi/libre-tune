package com.colux.libretune.data.local.mapper

import com.colux.libretune.data.local.entity.AlbumType
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.PlaylistEntity
import com.colux.libretune.data.local.wrapper.PlaylistWithArtists
import com.colux.libretune.data.model.Playlist
import com.colux.libretune.data.model.PlaylistType
import com.colux.libretune.data.model.wrapper.AlbumWithArtists


fun Playlist.toEntity(): PlaylistEntity {
    return PlaylistEntity(
        playlistId = this.id,
        name = this.name,
        images = this.images.map { it.toEntity() },
        releaseYear = this.releaseYear ?: 0,
        isLocal = this.isLocal,
        type = when (this.type) {
            PlaylistType.ALBUM -> AlbumType.ALBUM
            PlaylistType.SINGLE -> AlbumType.SINGLE
            PlaylistType.EP -> AlbumType.EP
            else -> AlbumType.PLAYLIST
        }
    )
}


fun PlaylistEntity.toDataModel(artists: List<ArtistEntity> = emptyList()): Playlist {
    return Playlist(
        id = this.playlistId,
        name = this.name,
        images = this.images.map { it.toDataModel() },
        artists = artists.map {
            it.toDataModel()
        },
        type = when (this.type) {
            AlbumType.ALBUM -> PlaylistType.ALBUM
            AlbumType.SINGLE -> PlaylistType.SINGLE
            AlbumType.EP -> PlaylistType.EP
            AlbumType.PLAYLIST -> PlaylistType.PLAYLIST
        },
        isLocal = this.isLocal ?: false,
        releaseYear = this.releaseYear
    )
}

fun PlaylistWithArtists.toDataModel(): Playlist {
    return Playlist(
        id = this.playlist.playlistId,
        name = this.playlist.name,
        releaseYear = this.playlist.releaseYear,
        images = this.playlist.images.map { it.toDataModel() },
        artists = this.artists.map { it.toDataModel() },
        type = when (this.playlist.type) {
            AlbumType.ALBUM -> PlaylistType.ALBUM
            AlbumType.SINGLE -> PlaylistType.SINGLE
            AlbumType.EP -> PlaylistType.EP
            AlbumType.PLAYLIST -> PlaylistType.PLAYLIST
        },
        isLocal = this.playlist.isLocal ?: false
    )
}

fun AlbumWithArtists.toDataModel(): Playlist {
    return Playlist(
        id = this.albumEntity.playlistId,
        name = this.albumEntity.name,
        releaseYear = this.albumEntity.releaseYear,
        images = this.albumEntity.images.map { it.toDataModel() },
        artists = this.artists.map { it.toDataModel() },
        type = when (this.albumEntity.type) {
            AlbumType.ALBUM -> PlaylistType.ALBUM
            AlbumType.SINGLE -> PlaylistType.SINGLE
            AlbumType.EP -> PlaylistType.EP
            AlbumType.PLAYLIST -> PlaylistType.PLAYLIST
        },
        isLocal = false
    )
}