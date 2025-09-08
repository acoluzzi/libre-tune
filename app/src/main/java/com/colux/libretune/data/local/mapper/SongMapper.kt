package com.colux.libretune.data.local.mapper

import com.colux.libretune.data.local.entity.SongEntity
import com.colux.libretune.data.local.wrapper.SongWithAlbumAndArtist
import com.colux.libretune.data.model.Song
import com.colux.libretune.data.model.wrapper.SongWithAlbumAndArtists

fun Song.toEntity(): SongEntity {
    return SongEntity(
        songId = id,
        title = title,
        images = images.map {
            it.toEntity()
        },
        albumId = album?.id,
        updateTimestamp = System.currentTimeMillis(),
        views = this.views,
        durationSec = this.durationSec,
        trackNumber = trackNumber
    )
}


fun SongWithAlbumAndArtists.toDataModel(): Song {
    return Song(
        id = this.songEntity.songId,
        title = this.songEntity.title,
        images = this.songEntity.images.map {
            it.toDataModel()
        },
        album = this.albumEntity?.toDataModel(this.artists),
        artists = this.artists.map {
            it.toDataModel()
        },
        views = this.songEntity.views,
        durationSec = this.songEntity.durationSec,
        trackNumber = this.songEntity.trackNumber
    )
}

fun SongWithAlbumAndArtist.toDataModel(): Song {
    return Song(
        id = this.song.songId,
        title = this.song.title,
        images = this.song.images.map {
            it.toDataModel()
        },
        album = this.album?.toDataModel(this.artists),
        artists = this.artists.map {
            it.toDataModel()
        },
        views = this.song.views,
        durationSec = this.song.durationSec,
        trackNumber = this.song.trackNumber
    )
}