package com.colux.libretune.data.local

import android.content.Context
import com.colux.libretune.data.local.dao.ArtistDao
import com.colux.libretune.data.local.dao.HistoryDao
import com.colux.libretune.data.local.dao.LibraryDao
import com.colux.libretune.data.local.dao.PlaylistDao
import com.colux.libretune.data.local.dao.SearchQueryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        callback: DatabaseCallback,
    ): AppDatabase = createAppDatabaseBuilder(context)
        .addCallback(callback)
        .build()

    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    @Provides
    @Singleton
    fun provideHistoryDao(database: AppDatabase): HistoryDao {
        return database.historyDao()
    }


    @Provides
    @Singleton
    fun provideLibraryDao(database: AppDatabase): LibraryDao {
        return database.libraryDao()
    }

    @Provides
    @Singleton
    fun provideSearchQueryDao(database: AppDatabase): SearchQueryDao {
        return database.searchQueryDao()
    }

    @Provides
    @Singleton
    fun provideDatabaseCallback(
        playlistDao: Provider<PlaylistDao>,
        scope: CoroutineScope
    ) = DatabaseCallback(playlistDao, scope)


    @Provides
    @Singleton
    fun provideLikedSongDao(database: AppDatabase) = database.songDao()


    @Provides
    @Singleton
    fun providePlaylistDao(database: AppDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    @Singleton
    fun provideArtistDao(database: AppDatabase): ArtistDao {
        return database.artistDao()
    }
}