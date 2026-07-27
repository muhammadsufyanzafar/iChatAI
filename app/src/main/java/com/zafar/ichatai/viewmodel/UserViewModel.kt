package com.zafar.ichatai.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zafar.ichatai.data.local.AppDatabase
import com.zafar.ichatai.data.local.UserPreferences
import com.zafar.ichatai.data.local.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class UserViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferences,
    private val database: AppDatabase
) : ViewModel() {

    private val userDao = database.userDao()

    // Using Flow from Room for better persistence
    val userProfile: StateFlow<UserEntity?> = userDao.getUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Bridge for components using old flows
    val userName: StateFlow<String> = userProfile.map { it?.name ?: userPreferences.getUserName() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, userPreferences.getUserName())
        
    val userEmail: StateFlow<String> = userProfile.map { it?.email ?: userPreferences.getUserEmail() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, userPreferences.getUserEmail())
        
    val gender: StateFlow<String> = userProfile.map { it?.gender ?: userPreferences.getGender() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, userPreferences.getGender())
        
    val avatarUri: StateFlow<String?> = userProfile.map { it?.avatarPath ?: userPreferences.getAvatarUri() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, userPreferences.getAvatarUri())

    init {
        // Migrate initial data from SharedPreferences to Room if Room is empty
        viewModelScope.launch {
            userDao.getUser().collect { user ->
                if (user == null) {
                    val initialUser = UserEntity(
                        name = userPreferences.getUserName(),
                        email = userPreferences.getUserEmail(),
                        gender = userPreferences.getGender(),
                        avatarPath = userPreferences.getAvatarUri()
                    )
                    userDao.insertUser(initialUser)
                }
            }
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: createDefaultUser()
            userDao.insertUser(current.copy(name = name))
            userPreferences.saveUserName(name)
        }
    }

    fun updateUserEmail(email: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: createDefaultUser()
            userDao.insertUser(current.copy(email = email))
            userPreferences.saveUserEmail(email)
        }
    }

    fun updateGender(gender: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: createDefaultUser()
            userDao.insertUser(current.copy(gender = gender))
            userPreferences.saveGender(gender)
        }
    }

    fun updateAvatarUri(uriString: String?) {
        viewModelScope.launch {
            val finalUri = if (uriString != null && !uriString.startsWith("res:")) {
                saveImageToInternalStorage(uriString)
            } else {
                uriString
            }
            
            val current = userProfile.value ?: createDefaultUser()
            userDao.insertUser(current.copy(avatarPath = finalUri))
            userPreferences.saveAvatarUri(finalUri)
        }
    }

    private suspend fun saveImageToInternalStorage(uriString: String): String? = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(uriString)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            
            val fileName = "user_avatar_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)
            
            // Delete old avatar files if they exist to save space
            context.filesDir.listFiles { _, name -> name.startsWith("user_avatar_") }?.forEach { it.delete() }
            
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createDefaultUser(): UserEntity {
        return UserEntity(
            name = "User${Random.nextInt(1000, 9999)}",
            email = "",
            gender = "Prefer not to say",
            avatarPath = null
        )
    }

    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            userPreferences.clearAllData()
            withContext(Dispatchers.IO) {
                database.clearAllTables()
                // Clear internal files
                context.filesDir.listFiles { _, name -> name.startsWith("user_avatar_") }?.forEach { it.delete() }
            }
            onComplete()
        }
    }

    fun isFirstRun() = userPreferences.isFirstRun()

    fun setFirstRunComplete() = userPreferences.setFirstRunComplete()
}
