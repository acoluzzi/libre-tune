package com.colux.libretune.data.model

import com.coluzziandrea.libretune_extractor.model.Playlist as ExtractorPlaylist

data class Playlist(val id: String, val name: String, val thumbnailUrl: String) {
    companion object {

        fun from(raw: ExtractorPlaylist): Playlist {
            return Playlist(
                id = raw.id,
                name = raw.name,
                thumbnailUrl = raw.images.firstOrNull()?.url ?: ""
            )
        }

    }

}
