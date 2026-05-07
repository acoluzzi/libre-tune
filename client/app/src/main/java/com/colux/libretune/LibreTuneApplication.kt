package com.colux.libretune

import android.app.Application
import androidx.work.WorkManager
import com.colux.libretune.data.sync.LibrarySyncWorker
import com.colux.libretune.di.androidSharedModule
import com.colux.libretune.di.appModule
import com.colux.libretune.di.coreSharedModule
import com.colux.libretune.di.jvmSharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class LibreTuneApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@LibreTuneApplication)
            workManagerFactory()
            modules(
                coreSharedModule,
                jvmSharedModule,
                androidSharedModule,
                appModule,
            )
        }

        val workManager = WorkManager.getInstance(this)
        LibrarySyncWorker.enqueuePeriodic(workManager)
        LibrarySyncWorker.runNow(workManager)
    }
}
