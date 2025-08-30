package com.colux.libretune.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.colux.libretune.data.local.entity.AlbumEntity
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.SongEntity
import com.colux.libretune.data.local.join.SongArtistCrossRef

data class SongWithArtistsAndAlbum(
    // This embeds the main Song object
    @Embedded
    val song: SongEntity,

    // This embeds the related Album object (one-to-one relationship from song's perspective)
    @Relation(
        parentColumn = "albumId", // The foreign key column in SongEntity
        entityColumn = "albumId"  // The primary key column in AlbumEntity
    )
    val album: AlbumEntity?,

    // This defines the many-to-many relationship between Song and Artist
    @Relation(
        parentColumn = "songId",
        entityColumn = "artistId",
        associateBy = Junction(SongArtistCrossRef::class)
    )
    val artists: List<ArtistEntity>
)