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
import java.io.IOException

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: CloudSyncRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Prevent background sync if we've already tried and failed recently
        if (runAttemptCount > 3) {
            return Result.failure()
        }

        val account = GoogleSignIn.getLastSignedInAccount(applicationContext)
            ?: return Result.failure()

        val syncResult = repository.backupToCloud(account)
        
        return if (syncResult.isSuccess) {
            Result.success()
        } else {
            val exception = syncResult.exceptionOrNull()
            
            // Check for specific non-retryable errors
            if (exception is IOException && exception.message?.contains("Conflict") == true) {
                // Conflict detected - background worker should NOT overwrite. 
                // Manual intervention needed.
                return Result.failure()
            }

            if (exception is GoogleJsonResponseException) {
                val statusCode = exception.statusCode
                // 401: Invalid Credentials, 403: Forbidden, 404: Not Found
                if (statusCode in listOf(401, 403, 404)) {
                    return Result.failure()
                }
            }

            // For transient network errors, retry with exponential backoff (handled by WorkManager)
            Result.retry()
        }
    }
}
