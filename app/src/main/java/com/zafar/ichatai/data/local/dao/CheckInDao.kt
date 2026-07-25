package com.zafar.ichatai.data.local.dao

import androidx.room.*
import com.zafar.ichatai.data.local.entity.CheckInStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Query("SELECT * FROM check_in_state WHERE id = 1")
    fun getCheckInState(): Flow<CheckInStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCheckInState(state: CheckInStateEntity)
}
