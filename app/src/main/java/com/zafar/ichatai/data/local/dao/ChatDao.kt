package com.zafar.ichatai.data.local.dao

import androidx.room.*
import com.zafar.ichatai.data.local.entity.ChatMessageEntity
import com.zafar.ichatai.data.local.entity.ChatSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY isPinned DESC, timestamp DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesListBySessionId(sessionId: Long): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: Long)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Query("UPDATE chat_sessions SET isPinned = :isPinned WHERE id = :sessionId")
    suspend fun updatePinnedStatus(sessionId: Long, isPinned: Boolean)

    @Query("""
        SELECT * FROM chat_sessions 
        WHERE title LIKE '%' || :query || '%' 
        OR id IN (SELECT sessionId FROM chat_messages WHERE content LIKE '%' || :query || '%')
        ORDER BY isPinned DESC, timestamp DESC
    """)
    fun searchSessions(query: String): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): ChatSessionEntity?

    @Query("SELECT COUNT(*) FROM chat_messages WHERE sessionId = :sessionId")
    fun getMessageCountForSession(sessionId: Long): Flow<Int>
}
