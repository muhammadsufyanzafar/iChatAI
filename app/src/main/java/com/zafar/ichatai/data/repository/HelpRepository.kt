package com.zafar.ichatai.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zafar.ichatai.data.FaqData
import com.zafar.ichatai.data.HelpCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class HelpRepository(private val context: Context) {
    suspend fun getFaqs(): List<FaqData> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.assets.open("faqs.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<FaqJson>>() {}.type
            val faqJsons: List<FaqJson> = Gson().fromJson(reader, type)
            faqJsons.map { it.toDomain() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private data class FaqJson(
        val question: String,
        val answer: String,
        val category: String
    ) {
        fun toDomain(): FaqData {
            return FaqData(
                question = question,
                answer = answer,
                category = try {
                    HelpCategory.valueOf(category)
                } catch (e: Exception) {
                    HelpCategory.GETTING_STARTED
                }
            )
        }
    }
}
