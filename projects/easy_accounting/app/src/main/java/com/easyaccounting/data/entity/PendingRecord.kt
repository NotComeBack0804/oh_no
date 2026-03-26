package com.easyaccounting.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 待确认账单表
 * 用于存储从支付宝/微信自动抓取的支付记录，等待用户确认后转为正式账单
 */
@Entity(tableName = "pending_records")
data class PendingRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,          // 金额
    val source: PaySource,       // 来源：ALIPAY / WECHAT
    val date: Long,             // 支付时间戳
    val createdAt: Long = System.currentTimeMillis(),
    val status: PendingStatus = PendingStatus.PENDING // 状态：PENDING / CONFIRMED / IGNORED
)

enum class PaySource {
    ALIPAY,   // 支付宝
    WECHAT,   // 微信
    MEITUAN,  // 美团
    DOUYIN,   // 抖音
    JD,       // 京东
    OTHER     // 其他
}

enum class PendingStatus {
    PENDING,   // 待确认
    CONFIRMED, // 已确认（已转为正式账单）
    IGNORED    // 已忽略
}
