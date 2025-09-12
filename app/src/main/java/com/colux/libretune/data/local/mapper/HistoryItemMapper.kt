package com.colux.libretune.data.local.mapper

import com.colux.libretune.data.local.wrapper.HistoryItemWithSongAlbumAndFirstArtist
import com.colux.libretune.data.model.HistoryItem
import com.colux.libretune.data.model.wrapper.SongWithAlbumAndArtists

fun HistoryItemWithSongAlbumAndFirstArtist.toDataModel() = HistoryItem(
    id = historyItem.historyId,
    song = song?.let {
        SongWithAlbumAndArtists(
            songEntity = song,
            albumEntity = album,
            artists = artists
        ).toDataModel()
    },
    playedAt = historyItem.playedAtTimestamp
)