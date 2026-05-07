package com.colux.libretune.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

/**
 * Periodic worker that asks [LibrarySyncOrchestrator] to reconcile the local
 * library with the LibreTune backend. The worker runs on a recurring schedule
 * and is also enqueued explicitly on app start.
 *
 * Constructed by Koin's WorkerFactory; the (Context, WorkerParameters) pair
 * is provided by WorkManager and the orchestrator is resolved from Koin.
 */
class LibrarySyncWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val orchestrator: LibrarySyncOrchestrator,
) : CoroutineWorker(context, workerParams) {

    private val logger = Logger.getLogger("LibrarySyncWorker")

    override suspend fun doWork(): Result {
        return orchestrator.syncAll().fold(
            onSuccess = { Result.success() },
            onFailure = { error ->
                logger.warning { "Sync failed, will retry: ${error.message}" }
                Result.retry()
            },
        )
    }

    companion object {
        private const val PERIODIC_NAME = "libretune-sync-periodic"
        private const val ONE_OFF_NAME = "libretune-sync-now"

        fun enqueuePeriodic(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<LibrarySyncWorker>(
                15, TimeUnit.MINUTES,
            ).setConstraints(constraints).build()
            workManager.enqueueUniquePeriodicWork(
                PERIODIC_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        fun runNow(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<LibrarySyncWorker>()
                .setConstraints(constraints)
                .build()
            workManager.enqueueUniqueWork(
                ONE_OFF_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }
    }
}
