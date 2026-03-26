package com.easyaccounting.ui.main

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.easyaccounting.data.entity.Account
import com.easyaccounting.databinding.ItemAccountBinding
import com.easyaccounting.util.IconUtils
import com.google.android.material.R as MaterialAttr
import com.google.android.material.color.MaterialColors

class AccountAdapter(
    private val onItemClick: (Account) -> Unit
) : ListAdapter<Account, AccountAdapter.AccountViewHolder>(AccountDiffCallback()) {

    private var selectedAccountId: Long? = null

    fun setSelectedAccount(accountId: Long?) {
        val oldSelected = selectedAccountId
        selectedAccountId = accountId
        currentList.forEachIndexed { index, account ->
            if (account.id == oldSelected || account.id == accountId) {
                notifyItemChanged(index)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = ItemAccountBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AccountViewHolder(
        private val binding: ItemAccountBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(account: Account) {
            binding.tvAccountName.text = account.name
            binding.ivAccountIcon.setImageResource(
                IconUtils.getAccountIconResourceId(account.type.name)
            )

            val selected = account.id == selectedAccountId
            val density = binding.root.resources.displayMetrics.density
            val strokeColor = if (selected) {
                MaterialColors.getColor(binding.root, MaterialAttr.attr.colorPrimary)
            } else {
                MaterialColors.getColor(binding.root, MaterialAttr.attr.colorOutlineVariant)
            }
            val backgroundColor = if (selected) {
                MaterialColors.getColor(binding.root, MaterialAttr.attr.colorPrimaryContainer)
            } else {
                MaterialColors.getColor(binding.root, MaterialAttr.attr.colorSurface)
            }

            binding.root.isSelected = selected
            binding.root.strokeColor = strokeColor
            binding.root.strokeWidth = if (selected) (2 * density).toInt() else 1
            binding.root.setCardBackgroundColor(backgroundColor)
            binding.root.cardElevation = if (selected) 6f * density else 0f
            binding.tvAccountName.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
        }
    }

    private class AccountDiffCallback : DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem == newItem
        }
    }
}
