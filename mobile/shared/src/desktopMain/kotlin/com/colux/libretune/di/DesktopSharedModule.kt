package com.colux.libretune.di

import com.colux.libretune.data.local.AppDatabase
import com.colux.libretune.data.local.DatabaseCallback
import com.colux.libretune.data.local.createAppDatabaseBuilder
import com.colux.libretune.data.remote.backend.BackendTokenStore
import com.colux.libretune.data.sync.SyncCollection
import com.colux.libretune.data.sync.SyncMetadataStore
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

/**
 * Desktop bindings. The token and sync-metadata stores are in-memory stubs
 * for now - Linux Desktop will get a proper file-backed implementation
 * when authenticated sync lands; today the desktop entry point is a stub
 * window so an in-memory fallback keeps the Koin graph satisfiable.
 */
val desktopSharedModule: Module = module {
    single<BackendTokenStore> { InMemoryBackendTokenStore() }
    single<SyncMetadataStore> { InMemorySyncMetadataStore() }
    single<AppDatabase> {
        val dbFile = File(System.getProperty("user.home"), ".libretune/libretune.db")
        createAppDatabaseBuilder(dbFile)
            .addCallback(get<DatabaseCallback>())
            .build()
    }
}

private class InMemoryBackendTokenStore : BackendTokenStore {
    private var token: String? = null
    override fun save(token: String) { this.token = token }
    override fun get(): String? = token
    override fun clear() { token = null }
    override fun isAuthenticated(): Boolean = !token.isNullOrEmpty()
}

private class InMemorySyncMetadataStore : SyncMetadataStore {
    private val local = mutableMapOf<SyncCollection, Long>()
    private val remote = mutableMapOf<SyncCollection, Long>()
    override fun setLocalChangedAt(collection: SyncCollection, timestamp: Long) {
        local[collection] = timestamp
    }
    override fun localChangedAt(collection: SyncCollection): Long = local[collection] ?: 0L
    override fun setRemoteUpdatedAt(collection: SyncCollection, timestamp: Long) {
        remote[collection] = timestamp
    }
    override fun remoteUpdatedAt(collection: SyncCollection): Long = remote[collection] ?: 0L
    override fun clear() { local.clear(); remote.clear() }
}
