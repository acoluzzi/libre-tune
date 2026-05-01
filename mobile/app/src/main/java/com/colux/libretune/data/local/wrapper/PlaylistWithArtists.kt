package com.colux.libretune.data.local.wrapper

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.PlaylistEntity
import com.colux.libretune.data.local.join.PlaylistArtistCrossRef

data class PlaylistWithArtists(
    @Embedded val playlist: PlaylistEntity,

    @Relation(
        parentColumn = "playlistId",
        entityColumn = "artistId",
        associateBy = Junction(PlaylistArtistCrossRef::class)
    )
    val artists: List<ArtistEntity>
)