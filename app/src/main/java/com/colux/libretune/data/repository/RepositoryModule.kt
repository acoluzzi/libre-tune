package com.colux.libretune.data.repository

import com.colux.libretune.data.local.dao.ArtistDao
import com.colux.libretune.data.remote.tube.YouTubeExtractionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    fun provideMusicRepositoryImpl(
        repository: YouTubeExtractionRepository,
        artistDao: ArtistDao
    ): MusicRepository {
        return MusicRepository(repository, artistDao)
    }

}