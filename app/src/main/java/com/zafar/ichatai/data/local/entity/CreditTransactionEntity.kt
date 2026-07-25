package com.zafar.ichatai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_transactions")
data class CreditTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "Daily Check In", "Ad Watched", etc.
    val amount: Int,
    val timestamp: Long
)
