package com.zafar.ichatai.data.repository

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zafar.ichatai.data.local.AppDatabase
import com.zafar.ichatai.data.local.UserPreferences
import com.zafar.ichatai.data.local.entity.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudSyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val userPreferences: UserPreferences
) {
    private val gson = Gson()

    suspend fun backupToCloud(account: GoogleSignInAccount): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(account)
            val backupData = mutableMapOf<String, Any>()

            if (userPreferences.isSyncHistoryEnabled()) {
                backupData["sessions"] = database.chatDao().getAllSessionsList()
                backupData["messages"] = database.chatDao().getAllMessagesList()
            }

            if (userPreferences.isSyncPromptsEnabled()) {
                backupData["prompts"] = database.promptDao().getAllSavedPromptsList()
                backupData["folders"] = database.promptDao().getAllFoldersList()
            }

            if (userPreferences.isSyncSettingsEnabled()) {
                database.userDao().getUserSync()?.let { backupData["user_profile"] = it }
                database.notificationPreferencesDao().getPreferencesSync()?.let { backupData["notification_prefs"] = it }
            }

            val json = gson.toJson(backupData)
            val contentStream = ByteArrayContent.fromString("application/json", json)

            val metadata = File()
                .setName("ichatai_backup.json")
                .setMimeType("application/json")
                .setParents(Collections.singletonList("appDataFolder"))

            val files = driveService.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = 'ichatai_backup.json'")
                .execute()

            if (files.files.isNotEmpty()) {
                val fileId = files.files[0].id
                driveService.files().update(fileId, metadata, contentStream).execute()
            } else {
                driveService.files().create(metadata, contentStream).execute()
            }

            userPreferences.setLastSyncTime(System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            userPreferences.appendSyncError(e.message ?: "Unknown backup error")
            Result.failure(e)
        }
    }

    suspend fun restoreFromCloud(account: GoogleSignInAccount): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(account)
            val files = driveService.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = 'ichatai_backup.json'")
                .execute()

            if (files.files.isEmpty()) {
                return@withContext Result.failure(Exception("No backup found in cloud"))
            }

            val fileId = files.files[0].id
            val outputStream = ByteArrayOutputStream()
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            
            val json = outputStream.toString()
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val backupData: Map<String, Any> = gson.fromJson(json, type)

            performRestore(backupData)
            
            userPreferences.setLastSyncTime(System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            userPreferences.appendSyncError(e.message ?: "Unknown restore error")
            Result.failure(e)
        }
    }
    
    private suspend fun performRestore(backupData: Map<String, Any>) = withContext(Dispatchers.IO) {
        if (userPreferences.isSyncSettingsEnabled()) {
            backupData["user_profile"]?.let {
                val user = gson.fromJson(gson.toJson(it), UserEntity::class.java)
                database.userDao().insertUser(user)
            }
            backupData["notification_prefs"]?.let {
                val prefs = gson.fromJson(gson.toJson(it), NotificationPreferencesEntity::class.java)
                database.notificationPreferencesDao().updatePreferences(prefs)
            }
        }

        if (userPreferences.isSyncHistoryEnabled()) {
            (backupData["sessions"] as? List<*>)?.forEach {
                val session = gson.fromJson(gson.toJson(it), ChatSessionEntity::class.java)
                database.chatDao().insertSession(session)
            }
            (backupData["messages"] as? List<*>)?.forEach {
                val message = gson.fromJson(gson.toJson(it), ChatMessageEntity::class.java)
                database.chatDao().insertMessage(message)
            }
        }

        if (userPreferences.isSyncPromptsEnabled()) {
            (backupData["folders"] as? List<*>)?.forEach {
                val folder = gson.fromJson(gson.toJson(it), PromptFolderEntity::class.java)
                database.promptDao().insertFolder(folder)
            }
            (backupData["prompts"] as? List<*>)?.forEach {
                val prompt = gson.fromJson(gson.toJson(it), SavedPromptEntity::class.java)
                database.promptDao().insertPrompt(prompt)
            }
        }
    }

    suspend fun deleteAllCloudData(account: GoogleSignInAccount): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(account)
            val files = driveService.files().list()
                .setSpaces("appDataFolder")
                .execute()

            for (file in files.files) {
                driveService.files().delete(file.id).execute()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            userPreferences.appendSyncError(e.message ?: "Error deleting cloud data")
            Result.failure(e)
        }
    }

    private fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, Collections.singleton(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("iChatAI").build()
    }
}
