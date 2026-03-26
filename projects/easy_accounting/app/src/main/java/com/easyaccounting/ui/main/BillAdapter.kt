package com.easyaccounting.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.easyaccounting.data.entity.BillWithCategory
import com.easyaccounting.databinding.ItemBillBinding
import com.easyaccounting.util.DateUtils
import com.easyaccounting.util.FormatUtils
import com.easyaccounting.util.IconUtils

class BillAdapter(
    private val onItemClick: (BillWithCategory) -> Unit,
    private val onItemLongClick: (BillWithCategory) -> Unit
) : ListAdapter<BillWithCategory, BillAdapter.BillViewHolder>(BillDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val binding = ItemBillBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BillViewHolder(
        private val binding: ItemBillBinding
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

        fun bind(billWithCategory: BillWithCategory) {
            binding.tvAmount.text = "- ${FormatUtils.formatAmount(billWithCategory.amount)}"
            binding.tvDate.text = DateUtils.formatDay(billWithCategory.date)
            binding.tvRemark.text = billWithCategory.remark?.takeIf { it.isNotBlank() } ?: "未填写备注"
            binding.tvCategory.text = billWithCategory.categoryName?.takeIf { it.isNotBlank() } ?: "未分类"
            binding.ivCategoryIcon.setImageResource(
                IconUtils.getCategoryIconByName(billWithCategory.categoryName)
            )
        }
    }

    private class BillDiffCallback : DiffUtil.ItemCallback<BillWithCategory>() {
        override fun areItemsTheSame(oldItem: BillWithCategory, newItem: BillWithCategory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BillWithCategory, newItem: BillWithCategory): Boolean {
            return oldItem == newItem
        }
    }
}
