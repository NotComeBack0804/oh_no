package com.easyaccounting.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.easyaccounting.data.entity.AiChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface AiChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: AiChatMessage): Long

    @Update
    suspend fun update(message: AiChatMessage)

    @Query(
        """
        SELECT * FROM ai_chat_messages
        WHERE conversationId = :conversationId
        ORDER BY createdAt ASC, id ASC
        """
    )
    fun getConversationMessages(conversationId: String): Flow<List<AiChatMessage>>

    @Query(
        """
        SELECT * FROM ai_chat_messages
        WHERE conversationId = :conversationId
        ORDER BY createdAt DESC, id DESC
        LIMIT :limit
        """
    )
    suspend fun getRecentMessages(conversationId: String, limit: Int): List<AiChatMessage>

    @Query(
        """
        DELETE FROM ai_chat_messages
        WHERE conversationId = :conversationId
        """
    )
    suspend fun clearConversation(conversationId: String)

    @Query(
        """
        SELECT COUNT(*) FROM ai_chat_messages
        WHERE linkedBillId = :billId
        """
    )
    suspend fun countByBillId(billId: Long): Int

    @Query(
        """
        SELECT COUNT(*) FROM ai_chat_messages
        WHERE linkedIncomeId = :incomeId
        """
    )
    suspend fun countByIncomeId(incomeId: Long): Int

    @Query(
        """
        SELECT id FROM ai_chat_messages
        WHERE linkedBillId = :billId
        ORDER BY createdAt ASC, id ASC
        LIMIT 1
        """
    )
    suspend fun findFirstMessageIdByBillId(billId: Long): Long?

    @Query(
        """
        SELECT id FROM ai_chat_messages
        WHERE linkedIncomeId = :incomeId
        ORDER BY createdAt ASC, id ASC
        LIMIT 1
        """
    )
    suspend fun findFirstMessageIdByIncomeId(incomeId: Long): Long?
}
