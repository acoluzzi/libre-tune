package com.colux.libretune.data.repository

import com.colux.libretune.data.local.AppDatabase
import com.colux.libretune.data.local.dao.SearchQueryDao
import com.colux.libretune.data.remote.backend.BackendApi
import com.colux.libretune.data.remote.backend.BackendTokenStore
import com.colux.libretune.data.remote.tube.YouTubeExtractionRepository
import com.colux.libretune.data.sync.AndroidSyncMetadataStore
import com.colux.libretune.data.sync.SyncMetadataStore
import com.coluzziandrea.libretune_extractor.LibreTuneExtractor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    fun provideSyncMetadataStore(impl: AndroidSyncMetadataStore): SyncMetadataStore = impl

    @Provides
    fun provideYouTubeExtractionRepository(
        extractor: LibreTuneExtractor,
    ): YouTubeExtractionRepository = YouTubeExtractionRepository(extractor)

    @Provides
    fun provideBackendSyncRepository(
        api: BackendApi,
        tokenStore: BackendTokenStore,
        syncMetadata: SyncMetadataStore,
    ): BackendSyncRepository = BackendSyncRepository(api, tokenStore, syncMetadata)

    @Provides
    fun provideArtistRepositoryImpl(
        remote: YouTubeExtractionRepository,
        db: AppDatabase,
        syncMetadata: SyncMetadataStore,
    ): ArtistRepository {
        return ArtistRepository(remote, db, syncMetadata)
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
        db: AppDatabase,
        syncMetadata: SyncMetadataStore,
    ): PlaylistRepository {
        return PlaylistRepository(remote, db, syncMetadata)
    }

    @Provides
    fun provideHomeRepositoryImpl(
        db: AppDatabase
    ): HomeRepository {
        return HomeRepository(db)
    }

    @Provides
    fun provideHistoryRepositoryImpl(
        db: AppDatabase
    ): HistoryRepository {
        return HistoryRepository(db)
    }

    @Provides
    fun provideAlbumRepositoryImpl(
        remote: YouTubeExtractionRepository,
        db: AppDatabase
    ): AlbumRepository {
        return AlbumRepository(remote, db)
    }


    @Provides
    fun provideSongRepositoryImpl(
        remote: YouTubeExtractionRepository,
        db: AppDatabase,
        syncMetadata: SyncMetadataStore,
    ): SongRepository {
        return SongRepository(remote, db, syncMetadata)
    }

}