package com.colux.libretune.data.model

import com.coluzziandrea.libretune_extractor.model.ArtistDetails as ExtractorArtistDetails

data class ArtistDetails(
    val name: String,
    val description: String?,
    val avatarUrl: String?,
    val bannerUrl: String?,
    val topSongs: List<Song>,
    val topSongPlaylist: Playlist? = null,
    val albums: List<Playlist>,
    val singlesAndEPs: List<Playlist>,
    val featuring: List<Playlist>,
    val playlists: List<Playlist>,
    val similarArtists: List<Artist>
) {
    companion object {
        fun from(raw: ExtractorArtistDetails): ArtistDetails {
            return ArtistDetails(
                name = raw.name,
                description = raw.description,
                avatarUrl = raw.images.minByOrNull { image ->
                    image.width
                }?.url,
                bannerUrl = raw.images.maxByOrNull { image ->
                    image.width
                }?.url,
                topSongPlaylist = raw.topSongsPlaylist?.let(Playlist.Companion::from),
                topSongs = raw.topSongs.map(Song.Companion::from),
                albums = raw.albums.map(Playlist::from),
                singlesAndEPs = raw.singlesAndEp.map(Playlist::from),
                featuring = raw.featuring.map(Playlist::from),
                playlists = raw.playlists.map(Playlist::from),
                similarArtists = raw.similarArtists.map(Artist::from)
            )
        }
    }
}