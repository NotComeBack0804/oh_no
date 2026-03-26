package com.easyaccounting.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ai_chat_messages",
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["linkedBillId"]),
        Index(value = ["linkedIncomeId"]),
        Index(value = ["createdAt"])
    ]
)
data class AiChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: String = DEFAULT_CONVERSATION_ID,
    val role: ChatMessageRole,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val intentType: AiIntentType = AiIntentType.CHAT,
    val ledgerAmount: Double? = null,
    val ledgerCategory: String? = null,
    val ledgerSummary: String? = null,
    val linkedBillId: Long? = null,
    val linkedIncomeId: Long? = null,
    val needsClarification: Boolean = false,
    val sourceLabel: String? = null
) {
    companion object {
        const val DEFAULT_CONVERSATION_ID = "main"
    }
}

enum class ChatMessageRole {
    USER,
    ASSISTANT
}

enum class AiIntentType {
    EXPENSE,
    INCOME,
    CHAT,
    CLARIFY
}
