package com.colux.libretune.data.model

data class ArtistDetails(
    val id: String,
    val name: String,
    val description: String?,
    val images: List<Image>,
    val topSongs: List<Song>,
    val albums: List<Playlist>,
    val singlesAndEPs: List<Playlist>,
    val featuring: List<Playlist>,
    val playlists: List<Playlist>,
    val similarArtists: List<Artist>,
    val discographyId: String? = null,
    val discographyAlbumsParam: String? = null,
    val discographySinglesParam: String? = null
) {

    fun getImageUrlForBanner(): String? {
        return images.maxByOrNull { it.width ?: 0 }?.url
    }

}