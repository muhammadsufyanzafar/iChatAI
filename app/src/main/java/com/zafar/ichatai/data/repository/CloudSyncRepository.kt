package com.zafar.ichatai.data.repository

import android.content.Context
import androidx.core.net.toUri
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zafar.ichatai.data.BackupPackage
import com.zafar.ichatai.data.SyncManifest
import com.zafar.ichatai.data.local.AppDatabase
import com.zafar.ichatai.data.local.UserPreferences
import com.zafar.ichatai.data.local.entity.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.MessageDigest
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
    private val BACKUP_FILE_NAME = "ichatai_backup_v2.json"

    suspend fun backupToCloud(account: GoogleSignInAccount): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(account)
            
            // 1. Conflict Detection: Check remote manifest first
            val remoteFile = findRemoteBackup(driveService)
            if (remoteFile != null) {
                val remotePackage = downloadBackup(driveService, remoteFile.id)
                if (remotePackage != null) {
                    val remoteManifest = remotePackage.manifest
                    if (remoteManifest.deviceId != userPreferences.getDeviceId() && 
                        remoteManifest.lastUpdated > userPreferences.getLastSyncTime()) {
                        throw IOException("Conflict detected: A newer backup exists from another device (${remoteManifest.deviceInfo}). Please restore first.")
                    }
                }
            }

            // 2. Prepare Payload
            val payload = mutableMapOf<String, Any>()
            if (userPreferences.isSyncHistoryEnabled()) {
                payload["sessions"] = database.chatDao().getAllSessionsList()
                payload["messages"] = database.chatDao().getAllMessagesList()
            }
            if (userPreferences.isSyncPromptsEnabled()) {
                payload["prompts"] = database.promptDao().getAllSavedPromptsList()
                payload["folders"] = database.promptDao().getAllFoldersList()
            }
            if (userPreferences.isSyncSettingsEnabled()) {
                database.userDao().getUserSync()?.let { payload["user_profile"] = it }
                database.notificationPreferencesDao().getPreferencesSync()?.let { payload["notification_prefs"] = it }
                payload["app_preferences"] = userPreferences.getAllPreferences()
                
                // Handle Profile Photo
                val avatarUri = userPreferences.getAvatarUri()
                if (avatarUri != null && avatarUri.startsWith("file")) {
                    try {
                        val uri = avatarUri.toUri()
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            val bytes = input.readBytes()
                            uploadAuxiliaryFile(driveService, "profile_avatar.jpg", "image/jpeg", bytes)
                            payload["has_custom_avatar"] = true
                        }
                    } catch (e: Exception) {
                        userPreferences.appendSyncError("Avatar upload skipped: ${e.message}")
                    }
                }
            }

            // 3. Create Manifest
            val stableJson = toStableJson(payload)
            val manifest = SyncManifest(
                version = 2,
                deviceId = userPreferences.getDeviceId(),
                lastUpdated = System.currentTimeMillis(),
                checksum = calculateMD5(stableJson)
            )
            
            val fullPackage = BackupPackage(manifest, payload)
            val fullJson = gson.toJson(fullPackage)
            val contentStream = ByteArrayContent.fromString("application/json", fullJson)

            // 4. Upload
            if (remoteFile != null) {
                val updateMetadata = File().setName(BACKUP_FILE_NAME).setMimeType("application/json")
                driveService.files().update(remoteFile.id, updateMetadata, contentStream).execute()
            } else {
                val createMetadata = File().setName(BACKUP_FILE_NAME).setMimeType("application/json")
                    .setParents(Collections.singletonList("appDataFolder"))
                driveService.files().create(createMetadata, contentStream).execute()
            }

            userPreferences.setLastSyncTime(manifest.lastUpdated)
            Result.success(Unit)
        } catch (e: Exception) {
            val diagnostic = formatDriveException("Backup failed", e)
            userPreferences.appendSyncError(diagnostic)
            Result.failure(IOException(diagnostic, e))
        }
    }

    suspend fun restoreFromCloud(account: GoogleSignInAccount): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(account)
            val remoteFile = findRemoteBackup(driveService) ?: return@withContext Result.failure(Exception("No backup found in cloud"))

            val backupPackage = downloadBackup(driveService, remoteFile.id) ?: throw IOException("Could not parse backup file")
            
            val currentChecksum = calculateMD5(toStableJson(backupPackage.payload))
            if (backupPackage.manifest.checksum != null && backupPackage.manifest.checksum != currentChecksum) {
                // If structure is readable, we allow restore but log a warning
                userPreferences.appendSyncError("Integrity check mismatch (expected ${backupPackage.manifest.checksum}, got $currentChecksum). Proceeding anyway.")
            }

            performRestore(driveService, backupPackage.payload)
            userPreferences.setLastSyncTime(backupPackage.manifest.lastUpdated)
            Result.success(Unit)
        } catch (e: Exception) {
            val diagnostic = formatDriveException("Restore failed", e)
            userPreferences.appendSyncError(diagnostic)
            Result.failure(IOException(diagnostic, e))
        }
    }

    private fun findRemoteBackup(driveService: Drive): File? {
        val files = driveService.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '$BACKUP_FILE_NAME' and trashed = false")
            .setFields("files(id,name,size,modifiedTime)")
            .execute()
        return files.files.firstOrNull()
    }

    private fun downloadBackup(driveService: Drive, fileId: String): BackupPackage? {
        val outputStream = ByteArrayOutputStream()
        driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
        val json = outputStream.toString(Charsets.UTF_8.name())
        val type = object : TypeToken<BackupPackage>() {}.type
        return gson.fromJson(json, type)
    }

    private suspend fun performRestore(driveService: Drive, payload: Map<String, Any>) = withContext(Dispatchers.IO) {
        // 1. Restore App Preferences (Themes, Model IDs, etc)
        (payload["app_preferences"] as? Map<*, *>)?.let {
            @Suppress("UNCHECKED_CAST")
            userPreferences.importPreferences(it as Map<String, *>)
        }

        // 2. Restore User Profile
        payload["user_profile"]?.let {
            val user = gson.fromJson(gson.toJson(it), UserEntity::class.java)
            database.userDao().insertUser(user)
        }
        payload["notification_prefs"]?.let {
            val prefs = gson.fromJson(gson.toJson(it), NotificationPreferencesEntity::class.java)
            database.notificationPreferencesDao().updatePreferences(prefs)
        }

        // 3. Restore Profile Photo
        if (payload["has_custom_avatar"] == true) {
            try {
                val bytes = downloadAuxiliaryFile(driveService, "profile_avatar.jpg")
                if (bytes != null) {
                    val file = java.io.File(context.filesDir, "restored_avatar.jpg")
                    file.writeBytes(bytes)
                    userPreferences.saveAvatarUri(file.toUri().toString())
                }
            } catch (e: Exception) {
                userPreferences.appendSyncError("Avatar restore failed: ${e.message}")
            }
        }

        // 4. Restore Chat History
        (payload["sessions"] as? List<*>)?.forEach {
            val session = gson.fromJson(gson.toJson(it), ChatSessionEntity::class.java)
            database.chatDao().insertSession(session)
        }
        (payload["messages"] as? List<*>)?.forEach {
            val message = gson.fromJson(gson.toJson(it), ChatMessageEntity::class.java)
            database.chatDao().insertMessage(message)
        }

        // 5. Restore Saved Prompts
        (payload["folders"] as? List<*>)?.forEach {
            val folder = gson.fromJson(gson.toJson(it), PromptFolderEntity::class.java)
            database.promptDao().insertFolder(folder)
        }
        (payload["prompts"] as? List<*>)?.forEach {
            val prompt = gson.fromJson(gson.toJson(it), SavedPromptEntity::class.java)
            database.promptDao().insertPrompt(prompt)
        }
    }

    suspend fun deleteAllCloudData(account: GoogleSignInAccount): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(account)
            val files = driveService.files().list()
                .setSpaces("appDataFolder")
                .setQ("trashed = false")
                .execute()

            for (file in files.files) {
                driveService.files().delete(file.id).execute()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            val diagnostic = formatDriveException("Cloud deletion failed", e)
            userPreferences.appendSyncError(diagnostic)
            Result.failure(IOException(diagnostic, e))
        }
    }

    suspend fun getBackupMetadata(account: GoogleSignInAccount): Result<Pair<Long, Long>> = withContext(Dispatchers.IO) {
        try {
            val driveService = getDriveService(account)
            val file = findRemoteBackup(driveService)
            if (file != null) {
                Result.success(Pair(file.getSize() ?: 0L, file.getModifiedTime()?.value ?: 0L))
            } else {
                Result.failure(Exception("No backup found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun downloadAuxiliaryFile(driveService: Drive, fileName: String): ByteArray? {
        val files = driveService.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '$fileName' and trashed = false")
            .execute()
        if (files.files.isEmpty()) return null
        val outputStream = ByteArrayOutputStream()
        driveService.files().get(files.files[0].id).executeMediaAndDownloadTo(outputStream)
        return outputStream.toByteArray()
    }

    private fun uploadAuxiliaryFile(driveService: Drive, fileName: String, mimeType: String, data: ByteArray) {
        val content = ByteArrayContent(mimeType, data)
        val existing = findRemoteFile(driveService, fileName)
        if (existing != null) {
            driveService.files().update(existing.id, null, content).execute()
        } else {
            val metadata = File().setName(fileName).setMimeType(mimeType)
                .setParents(Collections.singletonList("appDataFolder"))
            driveService.files().create(metadata, content).execute()
        }
    }

    private fun findRemoteFile(driveService: Drive, fileName: String): File? {
        return driveService.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '$fileName' and trashed = false")
            .execute().files.firstOrNull()
    }

    private fun getDriveService(account: GoogleSignInAccount): Drive {
        val androidAccount = account.account ?: throw IOException("Google Account details missing.")
        val credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE_APPDATA))
        credential.selectedAccount = androidAccount
        return Drive.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential).setApplicationName("iChatAI").build()
    }

    private fun calculateMD5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun toStableJson(obj: Any): String {
        // Sort keys to ensure deterministic JSON for checksums
        val element = gson.toJsonTree(obj)
        return sortJsonElement(element).toString()
    }

    private fun sortJsonElement(element: com.google.gson.JsonElement): com.google.gson.JsonElement {
        if (element.isJsonObject) {
            val obj = element.asJsonObject
            val sortedMap = java.util.TreeMap<String, com.google.gson.JsonElement>()
            for (entry in obj.entrySet()) {
                sortedMap[entry.key] = sortJsonElement(entry.value)
            }
            val sortedObj = com.google.gson.JsonObject()
            for (entry in sortedMap.entries) {
                sortedObj.add(entry.key, entry.value)
            }
            return sortedObj
        } else if (element.isJsonArray) {
            val array = element.asJsonArray
            val sortedArray = com.google.gson.JsonArray()
            for (i in 0 until array.size()) {
                sortedArray.add(sortJsonElement(array.get(i)))
            }
            return sortedArray
        }
        return element
    }

    private fun formatDriveException(operation: String, exception: Exception): String {
        val responseCode = (exception as? GoogleJsonResponseException)?.statusCode
        val responseMessage = (exception as? GoogleJsonResponseException)?.details?.message
        return buildString {
            append(operation)
            if (responseCode != null) append(" | http=$responseCode")
            append(" | type=${exception::class.java.simpleName}")
            val message = responseMessage ?: exception.message
            if (!message.isNullOrBlank()) append(" | message=$message")
        }
    }
}
