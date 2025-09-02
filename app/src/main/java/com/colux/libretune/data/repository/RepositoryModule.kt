package com.colux.libretune.data.repository

import com.colux.libretune.data.local.AppDatabase
import com.colux.libretune.data.local.dao.AlbumDao
import com.colux.libretune.data.local.dao.ArtistDao
import com.colux.libretune.data.local.dao.SearchQueryDao
import com.colux.libretune.data.remote.tube.YouTubeExtractionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    fun provideArtistRepositoryImpl(
        remote: YouTubeExtractionRepository,
        db: AppDatabase
    ): ArtistRepository {
        return ArtistRepository(remote, db)
    }

    @Provides
    fun provideSearchRepositoryImpl(
        remote: YouTubeExtractionRepository,
        searchQueryDao: SearchQueryDao,
    ): SearchRepository {
        return SearchRepository(remote, searchQueryDao)
    }

    @Provides
    fun providePlaylistRepositoryImpl(
        remote: YouTubeExtractionRepository,
        artistDao: ArtistDao,
        albumDao: AlbumDao
    ): PlaylistRepository {
        return PlaylistRepository(remote, artistDao, albumDao)
    }

    @Provides
    fun provideSongRepositoryImpl(
        remote: YouTubeExtractionRepository,
    ): SongRepository {
        return SongRepository(remote)
    }

}