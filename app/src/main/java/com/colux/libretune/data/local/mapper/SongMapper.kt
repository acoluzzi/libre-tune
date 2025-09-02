package com.colux.libretune.data.local.mapper

import com.colux.libretune.data.local.entity.SongEntity
import com.colux.libretune.data.local.wrapper.SongWithAlbumAndArtists
import com.colux.libretune.data.model.Song

fun Song.toEntity(): SongEntity {
    return SongEntity(
        songId = id,
        title = title,
        images = images.map {
            it.toEntity()
        },
        albumId = album?.id,
        updateTimestamp = System.currentTimeMillis()
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
        }
    )
}