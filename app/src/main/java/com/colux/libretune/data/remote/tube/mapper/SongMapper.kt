package com.colux.libretune.data.remote.tube.mapper

import com.colux.libretune.data.model.Song as DataModelSong
import com.coluzziandrea.libretune_extractor.model.Song as ExtractorSong

fun ExtractorSong.toDataModel(): DataModelSong {
    return DataModelSong(
        id = this.id,
        title = this.title,
        artists = this.artists.map { it.toDataModel() },
        album = this.album.let { it?.toDataModel() },
        images = this.images.map { it.toDataModel() }
    )
}