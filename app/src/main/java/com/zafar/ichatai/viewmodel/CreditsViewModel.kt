package com.zafar.ichatai.viewmodel

import android.app.Application
import android.app.Activity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.zafar.ichatai.data.local.AppDatabase
import com.zafar.ichatai.data.repository.CreditRepository
import com.zafar.ichatai.data.local.entity.CreditTransactionEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CreditsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CreditRepository
    
    val allTransactions: StateFlow<List<CreditTransactionEntity>>
    val totalCredits: StateFlow<Int>

    private var rewardedAd: RewardedAd? = null
    private val adUnitId = "ca-app-pub-3940256099942544/5224354917" // Test ID

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CreditRepository(database.creditDao())
        
        allTransactions = repository.allTransactions.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        totalCredits = repository.totalCredits.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), 0
        )
        
        loadRewardedAd()
    }

    fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(getApplication(), adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                rewardedAd = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
            }
        })
    }

    fun showRewardedAd(activity: Activity) {
        rewardedAd?.let { ad ->
            ad.show(activity) { rewardItem ->
                viewModelScope.launch {
                    repository.addCredits("Ad Watched", 20)
                }
            }
            rewardedAd = null
            loadRewardedAd()
        } ?: run {
            loadRewardedAd()
        }
    }
}
