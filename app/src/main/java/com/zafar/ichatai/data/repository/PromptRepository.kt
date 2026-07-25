package com.zafar.ichatai.data.repository

import com.zafar.ichatai.data.local.dao.PromptDao
import com.zafar.ichatai.data.local.entity.PromptFolderEntity
import com.zafar.ichatai.data.local.entity.PromptFolderWithCount
import com.zafar.ichatai.data.local.entity.SavedPromptEntity
import kotlinx.coroutines.flow.Flow

class PromptRepository(private val promptDao: PromptDao) {
    fun getAllFoldersWithCount(): Flow<List<PromptFolderWithCount>> = promptDao.getAllFoldersWithCount()
    
    fun getIndividualPrompts(): Flow<List<SavedPromptEntity>> = promptDao.getIndividualPrompts()
    
    fun getPromptsByFolder(folderId: Long): Flow<List<SavedPromptEntity>> = promptDao.getPromptsByFolder(folderId)
    
    fun searchPrompts(query: String): Flow<List<SavedPromptEntity>> = promptDao.searchPrompts(query)
    
    suspend fun insertFolder(folder: PromptFolderEntity): Long = promptDao.insertFolder(folder)
    
    suspend fun updateFolder(folder: PromptFolderEntity) = promptDao.updateFolder(folder)
    
    suspend fun deleteFolder(folder: PromptFolderEntity) = promptDao.deleteFolder(folder)
    
    suspend fun insertPrompt(prompt: SavedPromptEntity): Long = promptDao.insertPrompt(prompt)
    
    suspend fun updatePrompt(prompt: SavedPromptEntity) = promptDao.updatePrompt(prompt)
    
    suspend fun deletePrompt(prompt: SavedPromptEntity) = promptDao.deletePrompt(prompt)
    
    suspend fun updatePromptUsage(id: Long) = promptDao.updatePromptUsage(id)

    suspend fun saveRecentPrompt(content: String) = promptDao.upsertRecentPrompt(content)
}
