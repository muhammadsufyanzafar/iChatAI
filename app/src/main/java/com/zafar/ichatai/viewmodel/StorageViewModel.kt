package com.zafar.ichatai.viewmodel

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.text.format.Formatter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zafar.ichatai.data.local.AppDatabase
import com.zafar.ichatai.data.local.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class StorageUsageState(
    val conversationsSize: Long = 0L,
    val mediaSize: Long = 0L,
    val cacheSize: Long = 0L,
    val freeSpace: Long = 0L,
    val totalSpace: Long = 0L,
    val isLoading: Boolean = true,
    val isAutoCleanupEnabled: Boolean = false,
    val autoCleanupDays: Int = 30
) {
    val conversationsSizeStr: String get() = formatSize(conversationsSize)
    val mediaSizeStr: String get() = formatSize(mediaSize)
    val cacheSizeStr: String get() = formatSize(cacheSize)
    val freeSpaceStr: String get() = formatSize(freeSpace)
    val totalSpaceStr: String get() = formatSize(totalSpace)
    val usedSpace: Long get() = conversationsSize + mediaSize + cacheSize
    val usedSpaceStr: String get() = formatSize(usedSpace)

    private fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.2f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}

@HiltViewModel
class StorageViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _storageState = MutableStateFlow(StorageUsageState())
    val storageState: StateFlow<StorageUsageState> = _storageState.asStateFlow()

    init {
        refreshStorageUsage()
    }

    fun refreshStorageUsage() {
        viewModelScope.launch {
            _storageState.value = _storageState.value.copy(isLoading = true)
            val dbSize = getDatabaseSize()
            val mediaSize = getMediaSize()
            val cacheSize = getCacheSize()
            val (free, total) = getStorageInfo()

            _storageState.value = StorageUsageState(
                conversationsSize = dbSize,
                mediaSize = mediaSize,
                cacheSize = cacheSize,
                freeSpace = free,
                totalSpace = total,
                isLoading = false,
                isAutoCleanupEnabled = userPreferences.isAutoCleanupEnabled(),
                autoCleanupDays = userPreferences.getAutoCleanupDays()
            )
        }
    }

    fun toggleAutoCleanup(enabled: Boolean) {
        userPreferences.setAutoCleanupEnabled(enabled)
        _storageState.value = _storageState.value.copy(isAutoCleanupEnabled = enabled)
    }

    fun setAutoCleanupDays(days: Int) {
        userPreferences.setAutoCleanupDays(days)
        _storageState.value = _storageState.value.copy(autoCleanupDays = days)
    }

    private suspend fun getDatabaseSize(): Long = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath("ichat_database")
        if (dbFile.exists()) dbFile.length() else 0L
    }

    private suspend fun getMediaSize(): Long = withContext(Dispatchers.IO) {
        // Avatars and other user-generated files are usually in filesDir
        val filesDir = context.filesDir
        calculateDirectorySize(filesDir)
    }

    private suspend fun getCacheSize(): Long = withContext(Dispatchers.IO) {
        val internalCache = calculateDirectorySize(context.cacheDir)
        val externalCache = context.externalCacheDir?.let { calculateDirectorySize(it) } ?: 0L
        internalCache + externalCache
    }

    private fun calculateDirectorySize(directory: File): Long {
        var size: Long = 0
        directory.listFiles()?.forEach { file ->
            size += if (file.isDirectory) {
                calculateDirectorySize(file)
            } else {
                file.length()
            }
        }
        return size
    }

    private fun getStorageInfo(): Pair<Long, Long> {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        val totalBlocks = stat.blockCountLong
        return Pair(availableBlocks * blockSize, totalBlocks * blockSize)
    }

    fun clearCache(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteFiles(context.cacheDir)
            context.externalCacheDir?.let { deleteFiles(it) }
            refreshStorageUsage()
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    private fun deleteFiles(directory: File) {
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                deleteFiles(file)
            }
            file.delete()
        }
    }

    fun clearChatHistory(onComplete: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.chatDao().deleteAllMessages()
                database.chatDao().deleteAllSessions()
            }
            refreshStorageUsage()
            onComplete()
        }
    }

    suspend fun getAllMessages() = withContext(Dispatchers.IO) {
        database.chatDao().getAllMessages()
    }
}
