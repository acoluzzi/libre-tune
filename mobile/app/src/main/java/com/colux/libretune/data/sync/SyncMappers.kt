package com.colux.libretune.data.sync

import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.ImageAttribute
import com.colux.libretune.data.local.entity.PlaylistEntity
import com.colux.libretune.data.local.entity.SongEntity
import com.colux.libretune.data.remote.backend.RemoteAlbum
import com.colux.libretune.data.remote.backend.RemoteArtist
import com.colux.libretune.data.remote.backend.RemoteSong

internal fun List<ImageAttribute>.bestUrl(): String =
    maxByOrNull { (it.width ?: 0) * (it.height ?: 0) }?.url
        ?: firstOrNull()?.url
        ?: ""

internal fun SongEntity.toRemote(artistName: String, albumName: String): RemoteSong =
    RemoteSong(
        remote_id = songId,
        title = title,
        artist_name = artistName,
        album_name = albumName,
        duration_ms = (durationSec ?: 0L) * 1000L,
        thumbnail_url = images.bestUrl(),
    )

internal fun PlaylistEntity.toRemoteAlbum(artistName: String, position: Int): RemoteAlbum =
    RemoteAlbum(
        remote_id = playlistId,
        name = name,
        artist_name = artistName,
        thumbnail_url = images.bestUrl(),
        position = position,
    )

internal fun ArtistEntity.toRemoteArtist(position: Int): RemoteArtist =
    RemoteArtist(
        remote_id = artistId,
        name = name,
        thumbnail_url = images.bestUrl(),
        position = position,
    )
