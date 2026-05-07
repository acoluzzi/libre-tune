package com.colux.libretune.di

import com.colux.libretune.data.local.AppDatabase
import com.colux.libretune.data.local.DatabaseCallback
import com.colux.libretune.data.local.createAppDatabaseBuilder
import com.colux.libretune.data.remote.backend.BackendTokenStore
import com.colux.libretune.data.remote.backend.DesktopBackendTokenStore
import com.colux.libretune.data.sync.DesktopSyncMetadataStore
import com.colux.libretune.data.sync.SyncMetadataStore
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File

/**
 * Desktop bindings. The Android app builds its own Koin graph in :app; the
 * Desktop entry point can call [startKoin][org.koin.core.context.startKoin]
 * with [coreSharedModule] + [jvmSharedModule] + [desktopSharedModule] when it
 * eventually moves off the manual AppContainer.
 */
val desktopSharedModule: Module = module {
    single<String>(named(BACKEND_BASE_URL_QUALIFIER)) { "https://libretune.coluzziandrea.com" }
    single<BackendTokenStore> { DesktopBackendTokenStore() }
    single<SyncMetadataStore> { DesktopSyncMetadataStore() }
    single<AppDatabase> {
        val dbFile = File(System.getProperty("user.home"), ".local/share/libretune/libretune.db")
        createAppDatabaseBuilder(dbFile)
            .addCallback(get<DatabaseCallback>())
            .build()
    }
}
