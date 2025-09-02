package com.colux.libretune.data.remote.tube.mapper

import com.coluzziandrea.libretune_extractor.model.GenericMusicItem
import com.colux.libretune.data.model.SearchResult as DataModelSearchResult
import com.coluzziandrea.libretune_extractor.model.SearchResult as ExtractorSearchResult


fun ExtractorSearchResult.toDataModel(): DataModelSearchResult {
    val topSongs = genericMusicItems.mapNotNull {
        when (it) {
            is GenericMusicItem.SongResult -> it.song?.toDataModel()
            else -> null
        }
    }
    val topAlbums = genericMusicItems.mapNotNull {
        when (it) {
            is GenericMusicItem.AlbumResult -> it.album?.toDataModel()
            else -> null
        }
    }
    val topArtists = genericMusicItems.mapNotNull {
        when (it) {
            is GenericMusicItem.ArtistResult -> it.artist?.toDataModel()
            else -> null
        }
    }

    return DataModelSearchResult(
        topSongs = topSongs,
        topAlbums = topAlbums,
        topArtists = topArtists,
        songs = songs.map { it.toDataModel() },
        albums = albums.map { it.toDataModel() },
        artists = artists.map { it.toDataModel() },
        playlists = playlists.map { it.toDataModel() },
        communityPlaylists = communityPlaylists.map {
            it.toDataModel()
        }
    )
}