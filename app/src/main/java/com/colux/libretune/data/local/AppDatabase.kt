package com.colux.libretune.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.colux.libretune.data.local.dao.ArtistDao
import com.colux.libretune.data.local.dao.HistoryDao
import com.colux.libretune.data.local.dao.LibraryDao
import com.colux.libretune.data.local.dao.PlaylistDao
import com.colux.libretune.data.local.dao.SearchQueryDao
import com.colux.libretune.data.local.dao.SongDao
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.LibraryEntity
import com.colux.libretune.data.local.entity.PlaybackHistoryEntity
import com.colux.libretune.data.local.entity.PlaylistEntity
import com.colux.libretune.data.local.entity.SearchQueryEntity
import com.colux.libretune.data.local.entity.SongEntity
import com.colux.libretune.data.local.join.ArtistFeaturedPlaylistCrossRef
import com.colux.libretune.data.local.join.ArtistPlaylistCrossRef
import com.colux.libretune.data.local.join.ArtistRelatedCrossRef
import com.colux.libretune.data.local.join.PlaylistArtistCrossRef
import com.colux.libretune.data.local.join.PlaylistRelatedCrossRef
import com.colux.libretune.data.local.join.PlaylistSongCrossRef
import com.colux.libretune.data.local.join.SongArtistCrossRef

@Database(
    entities = [SongEntity::class,
        ArtistEntity::class,
        PlaylistEntity::class,
        ArtistRelatedCrossRef::class,
        PlaylistArtistCrossRef::class,
        SearchQueryEntity::class,
        ArtistPlaylistCrossRef::class,
        PlaylistRelatedCrossRef::class,
        ArtistFeaturedPlaylistCrossRef::class,
        SongArtistCrossRef::class,
        PlaybackHistoryEntity::class,
        LibraryEntity::class,
        PlaylistSongCrossRef::class], version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    abstract fun playlistDao(): PlaylistDao

    abstract fun artistDao(): ArtistDao


    abstract fun searchQueryDao(): SearchQueryDao

    abstract fun libraryDao(): LibraryDao
    abstract fun historyDao(): HistoryDao
}