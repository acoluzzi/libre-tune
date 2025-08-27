package com.coluzziandrea.libretune_extractor.model


data class ArtistDetails(
    val name: String,
    val description: String?,
    val bannerUrl: String?,
    val topSongs: List<Song>,
    val albums: List<Playlist>, // We'll represent albums as playlists
    val similarArtists: List<Artist>
)