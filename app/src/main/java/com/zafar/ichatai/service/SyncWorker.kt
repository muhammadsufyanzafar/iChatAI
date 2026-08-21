package com.zafar.ichatai.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.zafar.ichatai.data.repository.CloudSyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: CloudSyncRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
            ?: return Result.failure()

        val syncResult = repository.backupToCloud(account)
        if (syncResult.isSuccess) {
            return Result.success()
        }

        val exception = syncResult.exceptionOrNull()
        if (exception is GoogleJsonResponseException) {
            val statusCode = exception.statusCode
            // Do not repeatedly retry permanent authorization/configuration failures.
            if (statusCode in 400..499 && statusCode != 408 && statusCode != 429) {
                return Result.failure()
            }
        }

        return Result.retry()
    }
}
