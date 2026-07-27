package com.zafar.ichatai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zafar.ichatai.data.local.entity.NotificationPreferencesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationPreferencesDao {
    @Query("SELECT * FROM notification_preferences WHERE id = 0")
    fun getPreferences(): Flow<NotificationPreferencesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePreferences(preferences: NotificationPreferencesEntity)
}
