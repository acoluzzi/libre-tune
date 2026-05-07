package com.colux.libretune.di

import com.colux.libretune.data.local.AppDatabase
import com.colux.libretune.data.local.DatabaseCallback
import com.colux.libretune.data.local.createAppDatabaseBuilder
import com.colux.libretune.data.remote.backend.AndroidBackendTokenStore
import com.colux.libretune.data.remote.backend.BackendTokenStore
import com.colux.libretune.data.sync.AndroidSyncMetadataStore
import com.colux.libretune.data.sync.SyncMetadataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/** Android bindings that depend on the application Context. */
val androidSharedModule: Module = module {
    single<BackendTokenStore> { AndroidBackendTokenStore(androidContext()) }
    single<SyncMetadataStore> { AndroidSyncMetadataStore(androidContext()) }
    single<AppDatabase> {
        createAppDatabaseBuilder(androidContext())
            .addCallback(get<DatabaseCallback>())
            .build()
    }
}
