package com.colux.libretune.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.colux.libretune.data.local.dao.ArtistDao
import com.colux.libretune.data.local.dao.PlaylistDao
import com.colux.libretune.data.local.dao.SongDao
import com.colux.libretune.data.local.entity.AlbumEntity
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.PlaylistEntity
import com.colux.libretune.data.local.entity.SongEntity
import com.colux.libretune.data.local.join.AlbumArtistCrossRef
import com.colux.libretune.data.local.join.PlaylistSongCrossRef
import com.colux.libretune.data.local.join.SongArtistCrossRef

@Database(
    entities = [SongEntity::class,
        ArtistEntity::class,
        AlbumEntity::class,
        PlaylistEntity::class,
        SongArtistCrossRef::class,
        AlbumArtistCrossRef::class,
        PlaylistSongCrossRef::class], version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun likedSongDao(): SongDao

    abstract fun playlistDao(): PlaylistDao

    abstract fun artistDao(): ArtistDao
}