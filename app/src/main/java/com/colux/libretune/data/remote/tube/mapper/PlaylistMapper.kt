package com.colux.libretune.data.remote.tube.mapper

import com.colux.libretune.data.model.PlaylistType
import com.coluzziandrea.libretune_extractor.model.GenericMusicItem
import com.colux.libretune.data.model.Playlist as DataModelPlaylist
import com.coluzziandrea.libretune_extractor.model.Playlist as ExtractorPlaylist

fun ExtractorPlaylist.toDataModel(albumType: PlaylistType = PlaylistType.PLAYLIST): DataModelPlaylist {
    return DataModelPlaylist(
        id = this.id,
        name = this.name,
        images = this.images.map { it.toDataModel() },
        artists = this.artists?.map { it.toDataModel() } ?: emptyList(),
        type = albumType
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