package com.colux.libretune.data.remote.tube.mapper

import com.colux.libretune.data.model.PlaylistDetails
import com.colux.libretune.data.model.PlaylistType
import com.coluzziandrea.libretune_extractor.model.GenericMusicItem
import com.colux.libretune.data.model.Playlist as DataModelPlaylist
import com.coluzziandrea.libretune_extractor.model.Playlist as ExtractorPlaylist
import com.coluzziandrea.libretune_extractor.model.PlaylistDetails as ExtractorPlaylistDetails
import com.coluzziandrea.libretune_extractor.model.PlaylistType as ExtractorPlaylistType

fun ExtractorPlaylist.toDataModel(albumType: PlaylistType = PlaylistType.PLAYLIST): DataModelPlaylist {
    return DataModelPlaylist(
        id = this.id,
        name = this.name,
        images = this.images.map { it.toDataModel() },
        artists = this.artists?.map { it.toDataModel() } ?: emptyList(),
        releaseYear = this.releaseYear,
        type = when (this.type) {
            ExtractorPlaylistType.ALBUM -> PlaylistType.ALBUM
            ExtractorPlaylistType.SINGLE_EP -> PlaylistType.SINGLE_EP
            else -> albumType
        }
    )
}


fun ExtractorPlaylistDetails.toDataModel(): PlaylistDetails {
    return PlaylistDetails(
        name = this.name,
        images = this.images.map { it.toDataModel() },
        artists = this.artists.map { it.toDataModel() },
        type = when (this.type) {
            ExtractorPlaylistType.ALBUM -> PlaylistType.ALBUM
            ExtractorPlaylistType.SINGLE_EP -> PlaylistType.SINGLE_EP
            else -> PlaylistType.PLAYLIST
        },
        songs = this.songs.map { it.toDataModel() },
        relatedPlaylists = this.relatedPlaylists.map { it.toDataModel() }
    )
}


fun GenericMusicItem.toPlaylist(): DataModelPlaylist? {
    return when (this) {
        is GenericMusicItem.PlaylistResult -> this.playlist?.toDataModel()
        else -> null
    }
}

fun GenericMusicItem.toAlbum(): DataModelPlaylist? {
    return when (this) {
        is GenericMusicItem.AlbumResult -> this.album?.toDataModel()
        else -> null
    }
}