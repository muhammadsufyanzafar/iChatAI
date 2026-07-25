package com.zafar.ichatai.data.local.dao

import androidx.room.*
import com.zafar.ichatai.data.local.entity.CreditTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditDao {
    @Query("SELECT * FROM credit_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<CreditTransactionEntity>>

    @Insert
    suspend fun insertTransaction(transaction: CreditTransactionEntity)

    @Query("SELECT SUM(amount) FROM credit_transactions")
    fun getTotalCredits(): Flow<Int?>
}
