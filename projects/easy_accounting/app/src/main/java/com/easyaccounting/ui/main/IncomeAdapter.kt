package com.easyaccounting.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.easyaccounting.data.entity.Income
import com.easyaccounting.databinding.ItemIncomeBinding
import com.easyaccounting.util.DateUtils
import com.easyaccounting.util.FormatUtils

class IncomeAdapter(
    private val onItemClick: (Income) -> Unit,
    private val onItemLongClick: (Income) -> Unit
) : ListAdapter<Income, IncomeAdapter.IncomeViewHolder>(IncomeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val binding = ItemIncomeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IncomeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class IncomeViewHolder(
        private val binding: ItemIncomeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
            binding.root.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemLongClick(getItem(position))
                }
                true
            }
        }

        fun bind(income: Income) {
            binding.tvAmount.text = "+ ${FormatUtils.formatAmount(income.amount)}"
            binding.tvDate.text = DateUtils.formatDay(income.date)
            binding.tvSource.text = income.source
            binding.tvRemark.text = income.remark ?: ""
        }
    }

    private class IncomeDiffCallback : DiffUtil.ItemCallback<Income>() {
        override fun areItemsTheSame(oldItem: Income, newItem: Income): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Income, newItem: Income): Boolean {
            return oldItem == newItem
        }
    }
}
