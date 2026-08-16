package com.zafar.ichatai.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.zafar.ichatai.data.FaqData
import com.zafar.ichatai.data.HelpCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class HelpRepository(private val context: Context) {
    suspend fun getFaqs(): List<FaqData> = withContext(Dispatchers.IO) {
        try {
            val languageCode = context.resources.configuration.locales[0].language
            val fileName = when (languageCode) {
                "ar", "de", "es", "fr", "hi", "it", "pt", "ur", "zh" -> "faqs_$languageCode.json"
                else -> "faqs.json"
            }
            
            val inputStream = try {
                context.assets.open(fileName)
            } catch (e: Exception) {
                context.assets.open("faqs.json")
            }

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
        @SerializedName("question") val question: String,
        @SerializedName("answer") val answer: String,
        @SerializedName("category") val category: String
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
