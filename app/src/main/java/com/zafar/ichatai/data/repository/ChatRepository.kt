package com.zafar.ichatai.data.repository

import com.zafar.ichatai.data.local.dao.ChatDao
import com.zafar.ichatai.data.local.entity.ChatMessageEntity
import com.zafar.ichatai.data.local.entity.ChatSessionEntity
import com.zafar.ichatai.data.local.entity.ChatSessionWithCount
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {
    fun getAllSessions(): Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun createNewSession(title: String): Long {
        return chatDao.insertSession(
            ChatSessionEntity(
                title = title,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun saveMessage(message: ChatMessageEntity) {
        chatDao.insertMessage(message)
    }

    suspend fun saveMessages(messages: List<ChatMessageEntity>) {
        chatDao.insertMessages(messages)
    }

    suspend fun updateSession(session: ChatSessionEntity) {
        chatDao.insertSession(session)
    }

    suspend fun getMessagesListBySessionId(sessionId: Long): List<ChatMessageEntity> =
        chatDao.getMessagesListBySessionId(sessionId)

    fun searchSessions(query: String): Flow<List<ChatSessionWithCount>> =
        chatDao.searchSessionsWithCount(query)

    suspend fun deleteSession(sessionId: Long) {
        chatDao.deleteSession(sessionId)
    }

    suspend fun clearMessagesForSession(sessionId: Long) {
        chatDao.deleteMessagesForSession(sessionId)
    }

    suspend fun updatePinnedStatus(sessionId: Long, isPinned: Boolean) {
        chatDao.updatePinnedStatus(sessionId, isPinned)
    }

    suspend fun updateTopPinnedStatus(sessionId: Long, isTopPinned: Boolean) {
        chatDao.updateTopPinnedStatus(sessionId, isTopPinned)
    }

    suspend fun getSessionById(sessionId: Long): ChatSessionEntity? =
        chatDao.getSessionById(sessionId)

    fun getMessageCountForSession(sessionId: Long): Flow<Int> =
        chatDao.getMessageCountForSession(sessionId)

    fun searchFavoriteSessions(query: String): Flow<List<ChatSessionWithCount>> =
        chatDao.searchFavoriteSessions(query)
}
