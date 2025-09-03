package com.colux.libretune.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.colux.libretune.data.local.dao.AlbumDao
import com.colux.libretune.data.local.dao.ArtistDao
import com.colux.libretune.data.local.dao.PlaylistDao
import com.colux.libretune.data.local.dao.SearchQueryDao
import com.colux.libretune.data.local.dao.SongDao
import com.colux.libretune.data.local.entity.AlbumEntity
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.PlaylistEntity
import com.colux.libretune.data.local.entity.SearchQueryEntity
import com.colux.libretune.data.local.entity.SongEntity
import com.colux.libretune.data.local.join.AlbumArtistCrossRef
import com.colux.libretune.data.local.join.ArtistArtistCrossRef
import com.colux.libretune.data.local.join.ArtistFeaturedPlaylistCrossRef
import com.colux.libretune.data.local.join.ArtistPlaylistCrossRef
import com.colux.libretune.data.local.join.PlaylistSongCrossRef

@Database(
    entities = [SongEntity::class,
        ArtistEntity::class,
        AlbumEntity::class,
        PlaylistEntity::class,
        ArtistArtistCrossRef::class,
        AlbumArtistCrossRef::class,
        SearchQueryEntity::class,
        ArtistPlaylistCrossRef::class,
        ArtistFeaturedPlaylistCrossRef::class,
        PlaylistSongCrossRef::class], version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    abstract fun playlistDao(): PlaylistDao

    abstract fun artistDao(): ArtistDao

    abstract fun albumDao(): AlbumDao

    abstract fun searchQueryDao(): SearchQueryDao
}