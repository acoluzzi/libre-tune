package com.coluzziandrea.libretune_extractor.model


data class ArtistDetails(
    val name: String,
    val description: String?,
    val images: List<Image>,
    val topSongs: List<Song>,
    val albums: List<Playlist>,
    val singlesAndEp: List<Playlist>,
    val discographyId: String?,
    val discographyAlbumsParam: String?,
    val discographySinglesParam: String?,
    val featuring: List<Playlist>,
    val playlists: List<Playlist>,
    val similarArtists: List<Artist>
)