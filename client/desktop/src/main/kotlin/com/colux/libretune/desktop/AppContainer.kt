package com.colux.libretune.desktop

import com.colux.libretune.data.local.AppDatabase
import com.colux.libretune.data.local.DatabaseConstants
import com.colux.libretune.data.local.createAppDatabaseBuilder
import com.colux.libretune.data.local.entity.AlbumType
import com.colux.libretune.data.local.entity.PlaylistEntity
import com.colux.libretune.data.remote.backend.BackendApi
import com.colux.libretune.data.remote.backend.BackendTokenStore
import com.colux.libretune.data.remote.backend.DesktopBackendTokenStore
import com.colux.libretune.data.remote.tube.YouTubeExtractionRepository
import com.colux.libretune.data.repository.AlbumRepository
import com.colux.libretune.data.repository.ArtistRepository
import com.colux.libretune.data.repository.BackendSyncRepository
import com.colux.libretune.data.repository.HistoryRepository
import com.colux.libretune.data.repository.HomeRepository
import com.colux.libretune.data.repository.PlaylistRepository
import com.colux.libretune.data.repository.SearchRepository
import com.colux.libretune.data.repository.SongRepository
import com.colux.libretune.data.sync.DesktopSyncMetadataStore
import com.colux.libretune.data.sync.LibrarySyncOrchestrator
import com.colux.libretune.data.sync.SyncMetadataStore
import com.coluzziandrea.libretune_extractor.LibreTuneExtractor
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.util.logging.Logger

/**
 * Application-level dependency container for the desktop app.
 * Replaces Hilt for the JVM desktop target.
 */
object AppContainer {

    private val logger = Logger.getLogger("AppContainer")

    /** Shared application coroutine scope (cancelled only on process exit). */
    val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // ---------- Storage ----------

    private val dbFile: File = File(
        System.getProperty("user.home"),
        ".local/share/libretune/libretune.db",
    ).also { it.parentFile?.mkdirs() }

    val tokenStore: BackendTokenStore by lazy { DesktopBackendTokenStore() }

    val syncMetadataStore: SyncMetadataStore by lazy { DesktopSyncMetadataStore() }

    // ---------- Network ----------

    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val httpClient: HttpClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) { json(json) }
            install(Logging) { level = LogLevel.INFO }
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 30_000
            }
        }
    }

    // ---------- Database ----------

    val database: AppDatabase by lazy {
        createAppDatabaseBuilder(dbFile).build()
    }

    // ---------- Remote data sources ----------

    val backendApi: BackendApi by lazy {
        BackendApi(httpClient, tokenStore, BACKEND_BASE_URL)
    }

    val libreTuneExtractor: LibreTuneExtractor by lazy {
        LibreTuneExtractor(httpClient)
    }

    val extractionRepository: YouTubeExtractionRepository by lazy {
        YouTubeExtractionRepository(libreTuneExtractor)
    }

    // ---------- Repositories ----------

    val backendSyncRepository: BackendSyncRepository by lazy {
        BackendSyncRepository(backendApi, tokenStore, syncMetadataStore)
    }

    val albumRepository: AlbumRepository by lazy {
        AlbumRepository(extractionRepository, database)
    }

    val artistRepository: ArtistRepository by lazy {
        ArtistRepository(extractionRepository, database, syncMetadataStore)
    }

    val playlistRepository: PlaylistRepository by lazy {
        PlaylistRepository(extractionRepository, database, syncMetadataStore)
    }

    val songRepository: SongRepository by lazy {
        SongRepository(extractionRepository, database, syncMetadataStore)
    }

    val homeRepository: HomeRepository by lazy {
        HomeRepository(database)
    }

    val historyRepository: HistoryRepository by lazy {
        HistoryRepository(database)
    }

    val searchRepository: SearchRepository by lazy {
        SearchRepository(extractionRepository, database.searchQueryDao())
    }

    val librarySyncOrchestrator: LibrarySyncOrchestrator by lazy {
        LibrarySyncOrchestrator(backendSyncRepository, database, syncMetadataStore)
    }

    // ---------- Initialisation ----------

    /**
     * Must be called once at startup (before any UI is shown).
     * Seeds the "Liked Songs" playlist the first time the DB is opened,
     * and kicks off a background sync.
     */
    fun initialise() {
        // DB seed and background sync on IO
        appScope.launch(Dispatchers.IO) {
            seedDatabaseIfNeeded()
            runSyncIfAuthenticated()
        }
    }

    private suspend fun seedDatabaseIfNeeded() {
        val existing = database.playlistDao()
            .getPlaylistById(DatabaseConstants.LIKED_SONGS_PLAYLIST_ID)
        if (existing == null) {
            logger.info("Seeding 'Liked Songs' playlist…")
            database.playlistDao().upsert(
                PlaylistEntity(
                    playlistId = DatabaseConstants.LIKED_SONGS_PLAYLIST_ID,
                    name = DatabaseConstants.LIKED_SONGS_PLAYLIST_NAME,
                    images = emptyList(),
                    isLocal = true,
                    type = AlbumType.PLAYLIST,
                ),
            )
        }
    }

    private suspend fun runSyncIfAuthenticated() {
        if (tokenStore.isAuthenticated()) {
            logger.info("Running library sync…")
            librarySyncOrchestrator.syncAll()
        }
    }

    private const val BACKEND_BASE_URL = "https://libretune.coluzziandrea.com"
}
