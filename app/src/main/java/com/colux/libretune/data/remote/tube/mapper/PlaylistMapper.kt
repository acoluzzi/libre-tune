package com.colux.libretune.data.remote.tube.mapper

import com.colux.libretune.data.model.Playlist as DataModelPlaylist
import com.coluzziandrea.libretune_extractor.model.Playlist as ExtractorPlaylist

fun ExtractorPlaylist.toDataModel(): DataModelPlaylist {
    return DataModelPlaylist(
        id = this.id,
        name = this.name,
        images = this.images.map { it.toDataModel() },
        artists = this.artists?.map { it.toDataModel() } ?: emptyList(),
    )
}