package com.colux.libretune.data.local.wrapper

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.PlaybackHistoryEntity
import com.colux.libretune.data.local.entity.PlaylistEntity
import com.colux.libretune.data.local.entity.SongEntity
import com.colux.libretune.data.local.join.HistoryArtistCrossRef

data class HistoryItemWithSongAlbumAndFirstArtist(
    @Embedded
    val historyItem: PlaybackHistoryEntity,

    @Relation(
        parentColumn = "songId",
        entityColumn = "songId"
    )
    val song: SongEntity?,

    @Relation(
        parentColumn = "albumId",
        entityColumn = "playlistId"
    )
    val album: PlaylistEntity?,


    @Relation(
        parentColumn = "historyId",
        entityColumn = "artistId",
        associateBy = Junction(HistoryArtistCrossRef::class)
    )
    val artists: List<ArtistEntity>,
)