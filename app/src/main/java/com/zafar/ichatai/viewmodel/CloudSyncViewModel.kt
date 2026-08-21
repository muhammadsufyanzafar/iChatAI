package com.zafar.ichatai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.zafar.ichatai.data.local.UserPreferences
import com.zafar.ichatai.data.repository.CloudSyncRepository
import com.zafar.ichatai.service.SyncWorker
import com.zafar.ichatai.utils.NotificationHelper
import com.zafar.ichatai.utils.VibrationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class CloudSyncViewModel @Inject constructor(
    application: Application,
    private val repository: CloudSyncRepository,
    private val userPreferences: UserPreferences,
    private val vibrationHelper: VibrationHelper
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CloudSyncUiState())
    val uiState: StateFlow<CloudSyncUiState> = _uiState.asStateFlow()

    private val driveScope = Scope(DriveScopes.DRIVE_APPDATA)

    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(driveScope)
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(application, googleSignInOptions)
    private val workManager = WorkManager.getInstance(application)

    init {
        checkLastAccount()
        loadPreferences()
    }

    private fun checkLastAccount() {
        val account = GoogleSignIn.getLastSignedInAccount(getApplication())
        if (account != null && GoogleSignIn.hasPermissions(account, driveScope)) {
            _uiState.value = _uiState.value.copy(
                googleAccount = account,
                isDriveAuthorized = true
            )
        } else {
            _uiState.value = _uiState.value.copy(
                googleAccount = account,
                isDriveAuthorized = false
            )
        }
    }

    private fun loadPreferences() {
        _uiState.value = _uiState.value.copy(
            isAutoSyncEnabled = userPreferences.isCloudSyncEnabled(),
            isSyncOverWifiOnly = userPreferences.isSyncOverWifiOnly(),
            lastSyncTime = userPreferences.getLastSyncTime(),
            isSyncHistoryEnabled = userPreferences.isSyncHistoryEnabled(),
            isSyncImagesEnabled = userPreferences.isSyncImagesEnabled(),
            isSyncSettingsEnabled = userPreferences.isSyncSettingsEnabled(),
            isSyncPromptsEnabled = userPreferences.isSyncPromptsEnabled(),
            errorLog = userPreferences.getSyncErrorLog()
        )
    }

    fun getSignInIntent() = googleSignInClient.signInIntent

    fun handleSignInResult(account: GoogleSignInAccount?, exception: ApiException? = null) {
        if (account != null) {
            val hasDrivePermission = GoogleSignIn.hasPermissions(account, driveScope)
            _uiState.value = _uiState.value.copy(
                googleAccount = account,
                isDriveAuthorized = hasDrivePermission
            )

            if (hasDrivePermission) {
                if (_uiState.value.isAutoSyncEnabled) {
                    scheduleSync()
                }
            } else {
                recordAuthError(
                    "Google account selected, but Drive app-data permission was not granted. " +
                        "The account is connected but backup authorization is incomplete."
                )
            }
        } else {
            val statusCode = exception?.statusCode
            val statusName = statusCode?.let { GoogleSignInStatusCodes.getStatusCodeString(it) }
            val statusMessage = exception?.status?.statusMessage

            val diagnostic = buildString {
                append("Google sign-in failed")
                if (statusCode != null) append(" | code=$statusCode")
                if (!statusName.isNullOrBlank()) append(" | status=$statusName")
                if (!statusMessage.isNullOrBlank()) append(" | message=$statusMessage")
                append(" | package=com.zafar.ichatai")
            }

            recordAuthError(diagnostic)
            _uiState.value = _uiState.value.copy(
                googleAccount = null,
                isDriveAuthorized = false
            )
        }
    }

    private fun recordAuthError(message: String) {
        userPreferences.appendSyncError(message)
        _uiState.value = _uiState.value.copy(errorLog = userPreferences.getSyncErrorLog())
    }

    fun toggleAutoSync(enabled: Boolean) {
        userPreferences.setCloudSyncEnabled(enabled)
        _uiState.value = _uiState.value.copy(isAutoSyncEnabled = enabled)
        if (enabled && _uiState.value.isDriveAuthorized) {
            scheduleSync()
        } else if (!enabled) {
            workManager.cancelUniqueWork("cloud_sync_work")
        }
    }

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (userPreferences.isSyncOverWifiOnly()) NetworkType.UNMETERED
                else NetworkType.CONNECTED
            )
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "cloud_sync_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            syncRequest
        )
    }

    fun toggleSyncOverWifi(enabled: Boolean) {
        userPreferences.setSyncOverWifiOnly(enabled)
        _uiState.value = _uiState.value.copy(isSyncOverWifiOnly = enabled)
        if (_uiState.value.isAutoSyncEnabled && _uiState.value.isDriveAuthorized) {
            scheduleSync()
        }
    }

    fun toggleSyncHistory(enabled: Boolean) {
        userPreferences.setSyncHistoryEnabled(enabled)
        _uiState.value = _uiState.value.copy(isSyncHistoryEnabled = enabled)
    }

    fun toggleSyncImages(enabled: Boolean) {
        userPreferences.setSyncImagesEnabled(enabled)
        _uiState.value = _uiState.value.copy(isSyncImagesEnabled = enabled)
    }

    fun toggleSyncSettings(enabled: Boolean) {
        userPreferences.setSyncSettingsEnabled(enabled)
        _uiState.value = _uiState.value.copy(isSyncSettingsEnabled = enabled)
    }

    fun toggleSyncPrompts(enabled: Boolean) {
        userPreferences.setSyncPromptsEnabled(enabled)
        _uiState.value = _uiState.value.copy(isSyncPromptsEnabled = enabled)
    }

    fun syncNow() {
        val account = _uiState.value.googleAccount
        if (account == null || !_uiState.value.isDriveAuthorized) {
            recordAuthError("Cannot sync: Google account is not connected with Drive backup permission.")
            vibrationHelper.vibrateError()
            return
        }
        viewModelScope.launch {
            vibrationHelper.vibrateClick()
            _uiState.value = _uiState.value.copy(isSyncing = true)
            val result = repository.backupToCloud(account)
            if (result.isSuccess) {
                vibrationHelper.vibrateSuccess()
            } else {
                vibrationHelper.vibrateError()
                val error = result.exceptionOrNull()?.message ?: "Unknown backup error"
                NotificationHelper.showSyncErrorNotification(getApplication(), error)
            }
            _uiState.value = _uiState.value.copy(
                isSyncing = false,
                lastSyncTime = userPreferences.getLastSyncTime(),
                errorLog = userPreferences.getSyncErrorLog()
            )
        }
    }

    fun importFromCloud() {
        val account = _uiState.value.googleAccount
        if (account == null || !_uiState.value.isDriveAuthorized) {
            recordAuthError("Cannot import: Google account is not connected with Drive backup permission.")
            vibrationHelper.vibrateError()
            return
        }
        viewModelScope.launch {
            vibrationHelper.vibrateClick()
            _uiState.value = _uiState.value.copy(isSyncing = true)
            val result = repository.restoreFromCloud(account)
            if (result.isSuccess) {
                vibrationHelper.vibrateSuccess()
            } else {
                vibrationHelper.vibrateError()
                NotificationHelper.showSyncErrorNotification(getApplication(), result.exceptionOrNull()?.message ?: "Unknown restore error")
            }
            _uiState.value = _uiState.value.copy(
                isSyncing = false,
                errorLog = userPreferences.getSyncErrorLog()
            )
        }
    }

    fun deleteCloudData() {
        val account = _uiState.value.googleAccount ?: return
        if (!_uiState.value.isDriveAuthorized) {
            recordAuthError("Cannot delete cloud data: Drive backup permission is not granted.")
            return
        }
        viewModelScope.launch {
            repository.deleteAllCloudData(account)
            _uiState.value = _uiState.value.copy(errorLog = userPreferences.getSyncErrorLog())
        }
    }

    fun clearErrorLog() {
        userPreferences.clearSyncErrorLog()
        _uiState.value = _uiState.value.copy(errorLog = "")
    }
}

data class CloudSyncUiState(
    val googleAccount: GoogleSignInAccount? = null,
    val isDriveAuthorized: Boolean = false,
    val isSyncing: Boolean = false,
    val isAutoSyncEnabled: Boolean = false,
    val isSyncOverWifiOnly: Boolean = true,
    val lastSyncTime: Long = 0L,
    val isSyncHistoryEnabled: Boolean = true,
    val isSyncImagesEnabled: Boolean = false,
    val isSyncSettingsEnabled: Boolean = true,
    val isSyncPromptsEnabled: Boolean = true,
    val errorLog: String = ""
)
