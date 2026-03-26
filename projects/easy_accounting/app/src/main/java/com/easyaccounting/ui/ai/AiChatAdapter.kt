package com.easyaccounting.ui.ai

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.easyaccounting.data.entity.AiChatMessage
import com.easyaccounting.data.entity.ChatMessageRole
import com.easyaccounting.databinding.ItemAiMessageAssistantBinding
import com.easyaccounting.databinding.ItemAiMessageUserBinding
import com.google.android.material.R as MaterialAttr
import com.google.android.material.color.MaterialColors

class AiChatAdapter(
    private val onLedgerClick: (AiChatMessage) -> Unit
) : ListAdapter<AiChatMessage, RecyclerView.ViewHolder>(DiffCallback()) {

    private var highlightedMessageId: Long? = null

    fun setHighlightedMessageId(messageId: Long?) {
        val previous = highlightedMessageId
        highlightedMessageId = messageId

        previous?.let { oldId ->
            currentList.indexOfFirst { it.id == oldId }
                .takeIf { it >= 0 }
                ?.let(::notifyItemChanged)
        }

        messageId?.let { newId ->
            currentList.indexOfFirst { it.id == newId }
                .takeIf { it >= 0 }
                ?.let(::notifyItemChanged)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).role == ChatMessageRole.USER) {
            VIEW_TYPE_USER
        } else {
            VIEW_TYPE_ASSISTANT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_USER) {
            val binding = ItemAiMessageUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            UserViewHolder(binding)
        } else {
            val binding = ItemAiMessageAssistantBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            AssistantViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UserViewHolder -> holder.bind(getItem(position))
            is AssistantViewHolder -> holder.bind(getItem(position))
        }
    }

    inner class UserViewHolder(
        private val binding: ItemAiMessageUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: AiChatMessage) {
            binding.tvMessage.text = message.content
            binding.tvMeta.text = DateFormat.format("HH:mm", message.createdAt)
        }
    }

    inner class AssistantViewHolder(
        private val binding: ItemAiMessageAssistantBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: AiChatMessage) {
            binding.tvMessage.text = message.content
            binding.tvMeta.text = buildString {
                append(message.sourceLabel ?: "AI")
                append(" · ")
                append(DateFormat.format("HH:mm", message.createdAt))
            }

            binding.cardLedger.isVisible = !message.ledgerSummary.isNullOrBlank()
            binding.cardLedger.setOnClickListener(null)
            if (!message.ledgerSummary.isNullOrBlank()) {
                binding.tvLedgerSummary.text = message.ledgerSummary
                binding.cardLedger.setOnClickListener { onLedgerClick(message) }
            }

            val isHighlighted = message.id == highlightedMessageId
            binding.cardBubble.strokeColor = if (isHighlighted) {
                MaterialColors.getColor(binding.root, MaterialAttr.attr.colorPrimary)
            } else {
                MaterialColors.getColor(binding.root, MaterialAttr.attr.colorOutlineVariant)
            }
            binding.cardBubble.strokeWidth = if (isHighlighted) 3 else 1
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<AiChatMessage>() {
        override fun areItemsTheSame(oldItem: AiChatMessage, newItem: AiChatMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AiChatMessage, newItem: AiChatMessage): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_ASSISTANT = 2
    }
}
