package com.easyaccounting.util

import com.easyaccounting.R

object IconUtils {
    // 根据图标名称获取资源ID
    fun getIconResourceId(iconName: String): Int {
        return when (iconName) {
            "ic_food" -> R.drawable.ic_food
            "ic_transport" -> R.drawable.ic_transport
            "ic_shopping" -> R.drawable.ic_shopping
            "ic_entertainment" -> R.drawable.ic_entertainment
            "ic_medical" -> R.drawable.ic_medical
            "ic_education" -> R.drawable.ic_education
            "ic_salary" -> R.drawable.ic_salary
            "ic_bonus" -> R.drawable.ic_bonus
            "ic_investment" -> R.drawable.ic_investment
            "ic_other" -> R.drawable.ic_other
            "ic_alipay" -> R.drawable.ic_alipay
            "ic_wechat" -> R.drawable.ic_wechat
            "ic_bank" -> R.drawable.ic_bank
            "ic_cash" -> R.drawable.ic_cash
            else -> R.drawable.ic_other
        }
    }

    // 获取账户类型的图标
    fun getAccountIconResourceId(accountType: String): Int {
        return when (accountType) {
            "ALIPAY" -> R.drawable.ic_alipay
            "WECHAT" -> R.drawable.ic_wechat
            "BANK_CARD" -> R.drawable.ic_bank
            "CASH" -> R.drawable.ic_cash
            else -> R.drawable.ic_other
        }
    }
}
