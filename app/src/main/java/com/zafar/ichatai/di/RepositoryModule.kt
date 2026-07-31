package com.zafar.ichatai.di

import com.zafar.ichatai.data.local.dao.ChatDao
import com.zafar.ichatai.data.local.dao.CheckInDao
import com.zafar.ichatai.data.local.dao.CreditDao
import com.zafar.ichatai.data.local.dao.PromptDao
import com.zafar.ichatai.data.repository.ChatRepository
import com.zafar.ichatai.data.repository.CreditRepository
import com.zafar.ichatai.data.repository.HelpRepository
import com.zafar.ichatai.data.repository.PromptRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideHelpRepository(@ApplicationContext context: android.content.Context): HelpRepository {
        return HelpRepository(context)
    }

    @Provides
    @Singleton
    fun provideChatRepository(chatDao: ChatDao): ChatRepository {
        return ChatRepository(chatDao)
    }

    @Provides
    @Singleton
    fun provideCreditRepository(creditDao: CreditDao): CreditRepository {
        return CreditRepository(creditDao)
    }

    @Provides
    @Singleton
    fun providePromptRepository(promptDao: PromptDao): PromptRepository {
        return PromptRepository(promptDao)
    }
}
