package com.easyaccounting.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 支出账单表
 * 用于记录每笔支出
 */
@Entity(
    tableName = "bills",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["accountId"]),
        Index(value = ["date"]),
        Index(value = ["remark"])
    ]
)
data class Bill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val categoryId: Long?,
    val date: Long, // 时间戳（天），格式：yyyy-MM-dd 00:00:00
    val remark: String?,
    val accountId: Long?,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Bill with category name pre-joined for display purposes.
 * Used by BillAdapter to show category names without extra queries.
 */
data class BillWithCategory(
    val id: Long,
    val amount: Double,
    val categoryId: Long?,
    val categoryName: String?,
    val date: Long,
    val remark: String?,
    val accountId: Long?,
    val createdAt: Long
) {
    fun toBill(): Bill = Bill(
        id = id,
        amount = amount,
        categoryId = categoryId,
        date = date,
        remark = remark,
        accountId = accountId,
        createdAt = createdAt
    )
}
