package com.colux.libretune.data.local.wrapper

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.PlaylistEntity
import com.colux.libretune.data.local.entity.SongEntity
import com.colux.libretune.data.local.join.SongArtistCrossRef

class SongWithAlbumAndArtist(

    @Embedded
    val song: SongEntity,

    @Relation(
        parentColumn = "albumId", // The foreign key column in the SongEntity table
        entityColumn = "playlistId"  // The primary key column in the PlaylistEntity table
    )
    val album: PlaylistEntity?,

    // Defines the many-to-many relationship between a Song and its Artists.
    @Relation(
        parentColumn = "songId",
        entityColumn = "artistId",
        associateBy = Junction(SongArtistCrossRef::class)
    )
    val artists: List<ArtistEntity>
)