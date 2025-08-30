package com.colux.libretune.data.mapper

import com.colux.libretune.data.local.relation.ArtistWithSongsAndAlbums
import com.colux.libretune.data.model.ArtistDetails

fun ArtistWithSongsAndAlbums.toArtistDetails(): ArtistDetails {
    TODO()
}

/**
 * Converts the clean UI model (from the remote source) into the
 * complex database model that the DAO needs for insertion.
 */
fun ArtistDetails.toArtistWithSongsAndAlbums(): ArtistWithSongsAndAlbums {
    TODO()
}
