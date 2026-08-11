package com.zafar.ichatai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zafar.ichatai.data.local.dao.CheckInDao
import com.zafar.ichatai.data.local.entity.CheckInStateEntity
import com.zafar.ichatai.data.repository.CreditRepository
import com.zafar.ichatai.utils.VibrationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val checkInDao: CheckInDao,
    private val creditRepository: CreditRepository,
    private val vibrationHelper: VibrationHelper
) : ViewModel() {

    val checkInState: StateFlow<CheckInStateEntity> = checkInDao.getCheckInState()
        .map { it ?: CheckInStateEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CheckInStateEntity())

    fun performCheckIn() {
        viewModelScope.launch {
            val currentState = checkInState.value
            val now = System.currentTimeMillis()
            
            if (canCheckIn(currentState.lastCheckInMillis, now)) {
                val isConsecutive = isYesterday(currentState.lastCheckInMillis, now)
                val newStreak = if (isConsecutive) {
                    if (currentState.currentStreak >= 7) 1 else currentState.currentStreak + 1
                } else {
                    1
                }

                val rewardAmount = if (newStreak == 7) 100 else 10
                
                creditRepository.addCredits("Daily Check In (Day $newStreak)", rewardAmount)
                vibrationHelper.vibrateSuccess()
                checkInDao.updateCheckInState(
                    CheckInStateEntity(
                        lastCheckInMillis = now,
                        currentStreak = newStreak
                    )
                )
            }
        }
    }

    private fun canCheckIn(lastMillis: Long, currentMillis: Long): Boolean {
        if (lastMillis == 0L) return true
        val lastCal = Calendar.getInstance().apply { timeInMillis = lastMillis }
        val currentCal = Calendar.getInstance().apply { timeInMillis = currentMillis }
        
        return lastCal.get(Calendar.YEAR) != currentCal.get(Calendar.YEAR) ||
               lastCal.get(Calendar.DAY_OF_YEAR) != currentCal.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(lastMillis: Long, currentMillis: Long): Boolean {
        if (lastMillis == 0L) return false
        val lastCal = Calendar.getInstance().apply { timeInMillis = lastMillis }
        val yesterdayCal = Calendar.getInstance().apply {
            timeInMillis = currentMillis
            add(Calendar.DAY_OF_YEAR, -1)
        }
        
        return lastCal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR) &&
               lastCal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR)
    }
}
