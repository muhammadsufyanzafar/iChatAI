package com.zafar.ichatai.data.local.dao

import androidx.room.*
import com.zafar.ichatai.data.local.entity.PromptFolderEntity
import com.zafar.ichatai.data.local.entity.PromptFolderWithCount
import com.zafar.ichatai.data.local.entity.SavedPromptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {
    // Folders
    @Query("SELECT * FROM prompt_folders ORDER BY timestamp DESC")
    fun getAllFolders(): Flow<List<PromptFolderEntity>>

    @Query("""
        SELECT *, (SELECT COUNT(*) FROM saved_prompts WHERE folderId = f.id) as promptCount 
        FROM prompt_folders f
        ORDER BY timestamp DESC
    """)
    fun getAllFoldersWithCount(): Flow<List<PromptFolderWithCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: PromptFolderEntity): Long

    @Update
    suspend fun updateFolder(folder: PromptFolderEntity)

    @Delete
    suspend fun deleteFolder(folder: PromptFolderEntity)

    // Prompts
    @Query("SELECT * FROM saved_prompts ORDER BY lastUsed DESC")
    fun getAllPrompts(): Flow<List<SavedPromptEntity>>

    @Query("SELECT * FROM saved_prompts WHERE folderId = :folderId ORDER BY timestamp DESC")
    fun getPromptsByFolder(folderId: Long): Flow<List<SavedPromptEntity>>

    @Query("SELECT * FROM saved_prompts WHERE folderId IS NULL ORDER BY lastUsed DESC")
    fun getIndividualPrompts(): Flow<List<SavedPromptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: SavedPromptEntity): Long

    @Update
    suspend fun updatePrompt(prompt: SavedPromptEntity)

    @Delete
    suspend fun deletePrompt(prompt: SavedPromptEntity)

    @Query("SELECT * FROM saved_prompts WHERE content = :content LIMIT 1")
    suspend fun getPromptByContent(content: String): SavedPromptEntity?

    @Query("UPDATE saved_prompts SET lastUsed = :timestamp, usageCount = usageCount + 1 WHERE id = :id")
    suspend fun updatePromptUsage(id: Long, timestamp: Long = System.currentTimeMillis())

    @Transaction
    suspend fun upsertRecentPrompt(content: String) {
        val existing = getPromptByContent(content)
        if (existing != null) {
            updatePromptUsage(existing.id)
        } else {
            val title = if (content.length > 30) content.take(27) + "..." else content
            insertPrompt(SavedPromptEntity(title = title, content = content, tag = "Recent"))
        }
    }

    @Query("""
        SELECT * FROM saved_prompts 
        WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' 
        ORDER BY lastUsed DESC
    """)
    fun searchPrompts(query: String): Flow<List<SavedPromptEntity>>
}
