package com.easyaccounting.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 收入记录表
 * 用于记录每笔收入
 */
@Entity(
    tableName = "incomes",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["date"]),
        Index(value = ["remark"])
    ]
)
data class Income(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val source: String, // 收入来源
    val date: Long, // 时间戳（天）
    val remark: String?,
    val accountId: Long?,
    val createdAt: Long = System.currentTimeMillis()
)
