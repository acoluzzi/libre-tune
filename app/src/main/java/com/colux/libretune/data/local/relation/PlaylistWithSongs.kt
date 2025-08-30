package com.colux.libretune.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.colux.libretune.data.local.entity.PlaylistEntity
import com.colux.libretune.data.local.entity.SongEntity
import com.colux.libretune.data.local.join.PlaylistSongCrossRef

data class PlaylistWithSongs(
    // Tells Room to include all the fields of the PlaylistEntity directly in this object.
    @Embedded
    val playlist: PlaylistEntity,

    // This annotation defines the relationship between the tables.
    @Relation(
        parentColumn = "playlistId", // The primary key of the parent entity (PlaylistEntity).
        entityColumn = "songId",      // The primary key of the child entity (SongEntity).

        // This specifies the "join" table that links them for a many-to-many relationship.
        associateBy = Junction(PlaylistSongCrossRef::class)
    )
    val songs: List<SongEntity>
)