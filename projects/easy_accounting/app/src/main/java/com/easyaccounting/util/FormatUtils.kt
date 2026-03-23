package com.easyaccounting.util

import java.text.DecimalFormat

object FormatUtils {
    private val decimalFormat = DecimalFormat("#,##0.00")
    private val integerFormat = DecimalFormat("#,##0")

    /**
     * 格式化金额（保留两位小数）
     */
    fun formatAmount(amount: Double): String = decimalFormat.format(amount)

    /**
     * 格式化整数
     */
    fun formatInteger(num: Int): String = integerFormat.format(num)

    /**
     * 解析金额字符串
     */
    fun parseAmount(amountStr: String): Double {
        return try {
            amountStr.replace(",", "").toDouble()
        } catch (e: Exception) {
            0.0
        }
    }
}
