package com.easyaccounting.ui.main

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.easyaccounting.data.entity.Category
import com.easyaccounting.databinding.ItemCategoryBinding
import com.easyaccounting.util.IconUtils
import com.google.android.material.R as MaterialAttr
import com.google.android.material.color.MaterialColors

class CategoryAdapter(
    private val onItemClick: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    private var selectedCategoryId: Long? = null

    fun setSelectedCategory(categoryId: Long?) {
        val oldSelected = selectedCategoryId
        selectedCategoryId = categoryId
        currentList.forEachIndexed { index, category ->
            if (category.id == oldSelected || category.id == categoryId) {
                notifyItemChanged(index)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(category: Category) {
            binding.tvCategoryName.text = category.name
            binding.ivCategoryIcon.setImageResource(IconUtils.getIconResourceId(category.icon))

            val selected = category.id == selectedCategoryId
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
            binding.tvCategoryName.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
}
