package com.colux.libretune.data.model

import com.coluzziandrea.libretune_extractor.model.PlaylistDetails as ExtractorPlaylistDetails

data class PlaylistDetails(
    val name: String,
    val bannerUrl: String?,
    val artist: String,
    val songs: List<Song>,
    val relatedPlaylists: List<Playlist>
) {
    companion object {
        fun from(raw: ExtractorPlaylistDetails): PlaylistDetails {
            return PlaylistDetails(
                name = raw.name,
                artist = raw.artist,
                bannerUrl = raw.images.maxByOrNull { image ->
                    image.width
                }?.url,
                songs = raw.songs.map(Song.Companion::from),
                relatedPlaylists = raw.relatedPlaylists.map(Playlist::from),
            )
        }
    }
}