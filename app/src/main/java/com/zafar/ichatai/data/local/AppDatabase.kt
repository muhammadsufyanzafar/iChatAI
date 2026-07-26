package com.zafar.ichatai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zafar.ichatai.data.local.dao.ChatDao
import com.zafar.ichatai.data.local.dao.PromptDao
import com.zafar.ichatai.data.local.dao.CreditDao
import com.zafar.ichatai.data.local.dao.CheckInDao
import com.zafar.ichatai.data.local.dao.UserDao
import com.zafar.ichatai.data.local.entity.ChatMessageEntity
import com.zafar.ichatai.data.local.entity.ChatSessionEntity
import com.zafar.ichatai.data.local.entity.PromptFolderEntity
import com.zafar.ichatai.data.local.entity.SavedPromptEntity
import com.zafar.ichatai.data.local.entity.CreditTransactionEntity
import com.zafar.ichatai.data.local.entity.CheckInStateEntity
import com.zafar.ichatai.data.local.entity.UserEntity

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        PromptFolderEntity::class,
        SavedPromptEntity::class,
        CreditTransactionEntity::class,
        CheckInStateEntity::class,
        UserEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun promptDao(): PromptDao
    abstract fun creditDao(): CreditDao
    abstract fun checkInDao(): CheckInDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ichat_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
