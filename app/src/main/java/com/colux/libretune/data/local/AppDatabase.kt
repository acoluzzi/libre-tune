package com.colux.libretune.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
import com.colux.libretune.data.local.join.HistoryArtistCrossRef
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
        HistoryArtistCrossRef::class,
        PlaylistSongCrossRef::class], version = 2
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    abstract fun playlistDao(): PlaylistDao

    abstract fun artistDao(): ArtistDao


    abstract fun searchQueryDao(): SearchQueryDao

    abstract fun libraryDao(): LibraryDao
    abstract fun historyDao(): HistoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE playlists ADD COLUMN remotePlaylistId TEXT")
                db.execSQL("ALTER TABLE playlists ADD COLUMN syncEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE playlist_song_cross_ref ADD COLUMN setVideoId TEXT")
                db.execSQL(
                    "ALTER TABLE playlist_song_cross_ref ADD COLUMN syncState TEXT NOT NULL DEFAULT 'SYNCED'"
                )
            }
        }
    }
}
