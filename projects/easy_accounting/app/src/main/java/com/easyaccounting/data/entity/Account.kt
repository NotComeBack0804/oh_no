package com.easyaccounting.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 账户表
 * 用于管理用户的各个账户
 */
@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: Double = 0.0
)

enum class AccountType {
    ALIPAY,    // 支付宝
    WECHAT,    // 微信
    BANK_CARD, // 银行卡
    CASH       // 现金
}
