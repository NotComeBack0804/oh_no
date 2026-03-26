package com.easyaccounting.data.repository

import com.easyaccounting.data.dao.AiChatMessageDao
import com.easyaccounting.data.entity.AiChatMessage
import kotlinx.coroutines.flow.Flow

class AiChatRepository(
    private val aiChatMessageDao: AiChatMessageDao
) {
    fun getConversationMessages(conversationId: String): Flow<List<AiChatMessage>> {
        return aiChatMessageDao.getConversationMessages(conversationId)
    }

    suspend fun getRecentMessages(conversationId: String, limit: Int): List<AiChatMessage> {
        return aiChatMessageDao.getRecentMessages(conversationId, limit)
    }

    suspend fun insertMessage(message: AiChatMessage): Long {
        return aiChatMessageDao.insert(message)
    }

    suspend fun updateMessage(message: AiChatMessage) {
        aiChatMessageDao.update(message)
    }

    suspend fun clearConversation(conversationId: String) {
        aiChatMessageDao.clearConversation(conversationId)
    }

    suspend fun hasBillContext(billId: Long): Boolean {
        return aiChatMessageDao.countByBillId(billId) > 0
    }

    suspend fun hasIncomeContext(incomeId: Long): Boolean {
        return aiChatMessageDao.countByIncomeId(incomeId) > 0
    }

    suspend fun findFirstMessageIdByBillId(billId: Long): Long? {
        return aiChatMessageDao.findFirstMessageIdByBillId(billId)
    }

    suspend fun findFirstMessageIdByIncomeId(incomeId: Long): Long? {
        return aiChatMessageDao.findFirstMessageIdByIncomeId(incomeId)
    }
}
