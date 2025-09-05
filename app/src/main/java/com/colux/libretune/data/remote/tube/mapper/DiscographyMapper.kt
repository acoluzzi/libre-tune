package com.colux.libretune.data.remote.tube.mapper

import com.colux.libretune.data.model.Playlist
import com.coluzziandrea.libretune_extractor.model.Discography

fun Discography.toPlaylists(): List<Playlist> {
    return this.albums.map {
        it.toDataModel()
    }

}
