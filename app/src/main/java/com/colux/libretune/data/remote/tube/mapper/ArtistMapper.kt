package com.colux.libretune.data.remote.tube.mapper

import com.colux.libretune.data.model.ArtistDetails
import com.colux.libretune.data.model.PlaylistType
import com.coluzziandrea.libretune_extractor.model.GenericMusicItem
import com.colux.libretune.data.model.Artist as DataModelArtist
import com.coluzziandrea.libretune_extractor.model.Artist as ExtractorArtist
import com.coluzziandrea.libretune_extractor.model.ArtistDetails as ExtractorArtistDetails


fun ExtractorArtist.toDataModel(): DataModelArtist {
    return DataModelArtist(
        id = this.id,
        name = this.name,
        images = this.images.map { it.toDataModel() }
    )
}


fun GenericMusicItem.toArtist(): DataModelArtist? {
    return when (this) {
        is GenericMusicItem.ArtistResult -> this.artist?.toDataModel()
        else -> null
    }
}


fun ExtractorArtistDetails.toDataModel(): ArtistDetails {
    return ArtistDetails(
        name = name,
        description = description,
        images = images.map { image ->
            image.toDataModel()
        },
        topSongPlaylist = topSongsPlaylist?.toDataModel(),
        topSongs = topSongs.map {
            it.toDataModel()
        },
        albums = albums.map {
            it.toDataModel(PlaylistType.ALBUM)
        },
        singlesAndEPs = singlesAndEp.map {
            it.toDataModel(PlaylistType.SINGLE_EP)
        },
        featuring = featuring.map {
            it.toDataModel()
        },
        playlists = playlists.map {
            it.toDataModel()
        },
        similarArtists = similarArtists.map {
            it.toDataModel()
        }
    )
}