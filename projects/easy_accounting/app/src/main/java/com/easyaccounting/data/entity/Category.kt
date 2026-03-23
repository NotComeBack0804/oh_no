package com.easyaccounting.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 分类表
 * 用于收入和支出的分类
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String, // 图标名称或资源名
    val type: CategoryType,
    val isCustom: Boolean = false // 是否用户自定义
)

enum class CategoryType {
    EXPENSE, // 支出
    INCOME   // 收入
}
