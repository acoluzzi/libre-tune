package com.colux.libretune.data.model


data class ArtistDetails(
    val name: String,
    val description: String?,
    val avatarUrl: String?,
    val bannerUrl: String?,
    val topSongs: List<Song>,
    val albums: List<Album>,
    val similarArtists: List<Artist>
)