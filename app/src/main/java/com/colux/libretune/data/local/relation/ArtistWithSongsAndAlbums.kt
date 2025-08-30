package com.colux.libretune.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.colux.libretune.data.local.entity.AlbumEntity
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.SongEntity
import com.colux.libretune.data.local.join.AlbumArtistCrossRef
import com.colux.libretune.data.local.join.SongArtistCrossRef

data class ArtistWithSongsAndAlbums(
    @Embedded
    val artist: ArtistEntity,

    @Relation(
        parentColumn = "artistId",
        entityColumn = "albumId",
        associateBy = Junction(AlbumArtistCrossRef::class)
    )
    val albums: List<AlbumEntity>,

    @Relation(
        parentColumn = "artistId",
        entityColumn = "songId",
        associateBy = Junction(SongArtistCrossRef::class)
    )
    val songs: List<SongEntity>
)