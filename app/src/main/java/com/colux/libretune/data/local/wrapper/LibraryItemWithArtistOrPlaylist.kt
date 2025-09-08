package com.colux.libretune.data.local.wrapper

import androidx.room.Embedded
import androidx.room.Relation
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.LibraryEntity
import com.colux.libretune.data.local.entity.PlaylistEntity

data class LibraryItemWithArtistOrPlaylist(
    @Embedded
    val libraryItem: LibraryEntity,
    
    @Relation(
        parentColumn = "playlistId",
        entityColumn = "playlistId"
    )
    val playlist: PlaylistEntity?,

    @Relation(
        parentColumn = "artistId",
        entityColumn = "artistId"
    )
    val artist: ArtistEntity?
)