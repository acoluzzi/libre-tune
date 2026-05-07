package com.colux.libretune.di

import com.colux.libretune.data.local.AppDatabase
import com.colux.libretune.data.local.DatabaseCallback
import com.colux.libretune.data.local.dao.ArtistDao
import com.colux.libretune.data.local.dao.HistoryDao
import com.colux.libretune.data.local.dao.LibraryDao
import com.colux.libretune.data.local.dao.PlaylistDao
import com.colux.libretune.data.local.dao.SearchQueryDao
import com.colux.libretune.data.local.dao.SongDao
import com.colux.libretune.data.remote.backend.BackendApi
import com.colux.libretune.data.repository.BackendSyncRepository
import com.colux.libretune.data.repository.HistoryRepository
import com.colux.libretune.data.repository.SearchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Named String binding for the LibreTune backend's base URL. */
const val BACKEND_BASE_URL_QUALIFIER = "backendBaseUrl"

/** Named CoroutineScope binding for the application-wide lifetime. */
const val APPLICATION_SCOPE_QUALIFIER = "applicationScope"

/**
 * Bindings shared by every target. The platform modules (androidSharedModule,
 * desktopSharedModule) add the [AppDatabase], the [BackendTokenStore] /
 * [SyncMetadataStore] implementations, and the [BACKEND_BASE_URL_QUALIFIER]
 * value.
 */
val coreSharedModule: Module = module {
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            classDiscriminator = "type"
        }
    }
    single<CoroutineScope>(named(APPLICATION_SCOPE_QUALIFIER)) {
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    // DAO accessors derived from the platform-supplied AppDatabase.
    single<SongDao> { get<AppDatabase>().songDao() }
    single<PlaylistDao> { get<AppDatabase>().playlistDao() }
    single<ArtistDao> { get<AppDatabase>().artistDao() }
    single<SearchQueryDao> { get<AppDatabase>().searchQueryDao() }
    single<HistoryDao> { get<AppDatabase>().historyDao() }
    single<LibraryDao> { get<AppDatabase>().libraryDao() }

    // The lambda breaks the AppDatabase <-> PlaylistDao cycle.
    single<DatabaseCallback> {
        DatabaseCallback(
            playlistDao = { get() },
            scope = get(named(APPLICATION_SCOPE_QUALIFIER)),
        )
    }

    single<BackendApi> {
        BackendApi(
            httpClient = get(),
            tokenStore = get(),
            baseUrl = get(named(BACKEND_BASE_URL_QUALIFIER)),
        )
    }

    factory { SearchRepository(remote = get(), searchQueryDao = get()) }
    factory { HistoryRepository(db = get()) }
    factory { BackendSyncRepository(api = get(), tokenStore = get(), syncMetadata = get()) }
}
