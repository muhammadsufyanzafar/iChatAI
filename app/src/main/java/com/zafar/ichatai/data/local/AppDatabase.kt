package com.zafar.ichatai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zafar.ichatai.data.local.dao.ChatDao
import com.zafar.ichatai.data.local.entity.ChatMessageEntity
import com.zafar.ichatai.data.local.entity.ChatSessionEntity

@Database(entities = [ChatSessionEntity::class, ChatMessageEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ichat_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
