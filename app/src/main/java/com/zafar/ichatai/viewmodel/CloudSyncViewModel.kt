package com.zafar.ichatai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.zafar.ichatai.data.local.UserPreferences
import com.zafar.ichatai.data.repository.CloudSyncRepository
import com.zafar.ichatai.service.SyncWorker
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
    private val userPreferences: UserPreferences
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CloudSyncUiState())
    val uiState: StateFlow<CloudSyncUiState> = _uiState.asStateFlow()

    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(application, googleSignInOptions)
    private val workManager = WorkManager.getInstance(application)

    init {
        checkLastAccount()
        loadPreferences()
    }

    private fun checkLastAccount() {
        val account = GoogleSignIn.getLastSignedInAccount(getApplication())
        _uiState.value = _uiState.value.copy(googleAccount = account)
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

    fun handleSignInResult(account: GoogleSignInAccount?) {
        if (account != null) {
            _uiState.value = _uiState.value.copy(googleAccount = account)
            if (_uiState.value.isAutoSyncEnabled) {
                scheduleSync()
            }
        } else {
            // Log that sign in failed
            userPreferences.appendSyncError("Sign-in failed or was cancelled by user.")
            _uiState.value = _uiState.value.copy(googleAccount = null, errorLog = userPreferences.getSyncErrorLog())
        }
    }

    fun toggleAutoSync(enabled: Boolean) {
        userPreferences.setCloudSyncEnabled(enabled)
        _uiState.value = _uiState.value.copy(isAutoSyncEnabled = enabled)
        if (enabled) {
            scheduleSync()
        } else {
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
        if (_uiState.value.isAutoSyncEnabled) {
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
        if (account == null) {
            userPreferences.appendSyncError("Cannot sync: No Google account connected.")
            _uiState.value = _uiState.value.copy(errorLog = userPreferences.getSyncErrorLog())
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            val result = repository.backupToCloud(account)
            _uiState.value = _uiState.value.copy(
                isSyncing = false,
                lastSyncTime = userPreferences.getLastSyncTime(),
                errorLog = userPreferences.getSyncErrorLog()
            )
        }
    }

    fun importFromCloud() {
        val account = _uiState.value.googleAccount
        if (account == null) {
            userPreferences.appendSyncError("Cannot import: No Google account connected.")
            _uiState.value = _uiState.value.copy(errorLog = userPreferences.getSyncErrorLog())
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            val result = repository.restoreFromCloud(account)
            _uiState.value = _uiState.value.copy(
                isSyncing = false,
                errorLog = userPreferences.getSyncErrorLog()
            )
        }
    }

    fun deleteCloudData() {
        val account = _uiState.value.googleAccount ?: return
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
