package com.zafar.ichatai.di

import android.content.Context
import com.zafar.ichatai.data.local.AppDatabase
import com.zafar.ichatai.data.local.dao.ChatDao
import com.zafar.ichatai.data.local.dao.CheckInDao
import com.zafar.ichatai.data.local.dao.CreditDao
import com.zafar.ichatai.data.local.dao.PromptDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideChatDao(database: AppDatabase): ChatDao = database.chatDao()

    @Provides
    fun providePromptDao(database: AppDatabase): PromptDao = database.promptDao()

    @Provides
    fun provideCreditDao(database: AppDatabase): CreditDao = database.creditDao()

    @Provides
    fun provideCheckInDao(database: AppDatabase): CheckInDao = database.checkInDao()
}
