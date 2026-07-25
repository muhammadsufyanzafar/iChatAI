package com.zafar.ichatai.di

import android.content.Context
import com.zafar.ichatai.utils.NetworkObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNetworkObserver(@ApplicationContext context: Context): NetworkObserver {
        return NetworkObserver(context)
    }
}
