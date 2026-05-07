package com.colux.libretune.di

import com.colux.libretune.data.remote.tube.YouTubeExtractionRepository
import com.colux.libretune.data.repository.AlbumRepository
import com.colux.libretune.data.repository.ArtistRepository
import com.colux.libretune.data.repository.HomeRepository
import com.colux.libretune.data.repository.PlaylistRepository
import com.colux.libretune.data.repository.SongRepository
import com.colux.libretune.data.sync.LibrarySyncOrchestrator
import com.coluzziandrea.libretune_extractor.LibreTuneExtractor
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Bindings shared by Android and Desktop. Both targets are JVM and use the
 * Ktor CIO engine, so the HTTP setup lives here rather than per-platform.
 */
val jvmSharedModule: Module = module {
    single<HttpClient> {
        HttpClient(CIO) {
            install(ContentNegotiation) { json(get()) }
            install(Logging) { level = LogLevel.ALL }
        }
    }

    single { LibreTuneExtractor(httpClient = get()) }
    single { YouTubeExtractionRepository(libreTuneExtractor = get()) }

    factory { AlbumRepository(remote = get(), db = get()) }
    factory { HomeRepository(db = get()) }
    factory { ArtistRepository(remote = get(), db = get(), syncMetadata = get()) }
    factory { PlaylistRepository(remote = get(), db = get(), syncMetadata = get()) }
    factory { SongRepository(remote = get(), db = get(), syncMetadata = get()) }

    single { LibrarySyncOrchestrator(backend = get(), db = get(), metadata = get()) }
}
