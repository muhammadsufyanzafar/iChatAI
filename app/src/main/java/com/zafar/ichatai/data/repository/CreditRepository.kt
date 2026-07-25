package com.zafar.ichatai.data.repository

import com.zafar.ichatai.data.local.dao.CreditDao
import com.zafar.ichatai.data.local.entity.CreditTransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CreditRepository(private val creditDao: CreditDao) {
    val allTransactions: Flow<List<CreditTransactionEntity>> = creditDao.getAllTransactions()
    val totalCredits: Flow<Int> = creditDao.getTotalCredits().map { it ?: 0 }

    suspend fun addCredits(type: String, amount: Int) {
        val transaction = CreditTransactionEntity(
            type = type,
            amount = amount,
            timestamp = System.currentTimeMillis()
        )
        creditDao.insertTransaction(transaction)
    }
}
