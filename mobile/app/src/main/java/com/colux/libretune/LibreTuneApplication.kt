package com.colux.libretune

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.colux.libretune.data.sync.LibrarySyncWorker
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject

@HiltAndroidApp
class LibreTuneApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        val workManager = WorkManager.getInstance(this)
        LibrarySyncWorker.enqueuePeriodic(workManager)
        LibrarySyncWorker.runNow(workManager)
    }
}
