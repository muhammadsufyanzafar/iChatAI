package com.zafar.ichatai.viewmodel

import android.app.Application
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.api.services.drive.DriveScopes
import com.zafar.ichatai.R
import com.zafar.ichatai.data.local.UserPreferences
import com.zafar.ichatai.data.repository.CloudSyncRepository
import com.zafar.ichatai.service.SyncWorker
import com.zafar.ichatai.utils.NotificationHelper
import com.zafar.ichatai.utils.VibrationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
        // .requestScopes(driveScope) // Temporarily disabled
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(application, googleSignInOptions)
    private val workManager = WorkManager.getInstance(application)
    private val firebaseAuth = FirebaseAuth.getInstance()

    init {
        checkLastAccount()
        loadPreferences()
        refreshMetadata()
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

    fun refreshMetadata() {
        val account = _uiState.value.googleAccount ?: return
        if (!_uiState.value.isDriveAuthorized) return

        viewModelScope.launch {
            repository.getBackupMetadata(account).onSuccess { (size, time) ->
                _uiState.update { it.copy(
                    backupSize = formatFileSize(size),
                    lastSyncTime = if (time > 0) time else it.lastSyncTime,
                    syncStatus = "Synced"
                ) }
            }.onFailure {
                _uiState.update { it.copy(syncStatus = "Not Synced") }
            }
        }
    }

    private fun formatFileSize(size: Long): String {
        if (size <= 0) return "0 KB"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return java.text.DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
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
            // Firebase Auth Integration
            val idToken = account.idToken
            if (idToken != null) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        recordAuthError("Firebase Auth failed: ${task.exception?.message}")
                    }
                }
            }

            val hasDrivePermission = GoogleSignIn.hasPermissions(account, driveScope)
            _uiState.update { it.copy(
                googleAccount = account,
                isDriveAuthorized = hasDrivePermission
            ) }

            if (hasDrivePermission) {
                refreshMetadata()
                if (_uiState.value.isAutoSyncEnabled) {
                    scheduleSync()
                }
            }
        } else {
            val statusCode = exception?.statusCode ?: -1
            val statusName = GoogleSignInStatusCodes.getStatusCodeString(statusCode)
            val statusMessage = exception?.status?.statusMessage ?: "No detailed message"
            
            val diagnostic = buildString {
                append("Sign-in failed (Code $statusCode: $statusName)\n")
                append("Reason: $statusMessage\n")
                
                // Diagnostic: Check signature at runtime
                try {
                    val pm = getApplication<Application>().packageManager
                    val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        pm.getPackageInfo(getApplication<Application>().packageName, android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES)
                    } else {
                        @Suppress("DEPRECATION")
                        pm.getPackageInfo(getApplication<Application>().packageName, android.content.pm.PackageManager.GET_SIGNATURES)
                    }
                    
                    val signatures = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        packageInfo.signingInfo.apkContentsSigners
                    } else {
                        @Suppress("DEPRECATION")
                        packageInfo.signatures
                    }

                    for (sig in signatures) {
                        val md = java.security.MessageDigest.getInstance("SHA-1")
                        val digest = md.digest(sig.toByteArray())
                        val hexString = digest.joinToString(":") { "%02X".format(it) }
                        append("Runtime SHA-1: $hexString\n")
                    }
                } catch (e: Exception) {
                    append("Could not read Runtime SHA-1: ${e.message}\n")
                }
                
                append("Package: ${getApplication<Application>().packageName}\n")
            }

            recordAuthError(diagnostic)
            _uiState.update { it.copy(googleAccount = null, isDriveAuthorized = false) }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            googleSignInClient.signOut()
            firebaseAuth.signOut()
            _uiState.update { it.copy(
                googleAccount = null,
                isDriveAuthorized = false,
                backupSize = "0 KB",
                syncStatus = "Not Synced"
            ) }
            workManager.cancelUniqueWork("cloud_sync_work")
        }
    }

    private fun recordAuthError(message: String) {
        userPreferences.appendSyncError(message)
        _uiState.update { it.copy(errorLog = userPreferences.getSyncErrorLog()) }
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

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(24, TimeUnit.HOURS)
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
    val backupSize: String = "0 KB",
    val syncStatus: String = "Not Synced",
    val isSyncHistoryEnabled: Boolean = true,
    val isSyncImagesEnabled: Boolean = false,
    val isSyncSettingsEnabled: Boolean = true,
    val isSyncPromptsEnabled: Boolean = true,
    val errorLog: String = ""
)
