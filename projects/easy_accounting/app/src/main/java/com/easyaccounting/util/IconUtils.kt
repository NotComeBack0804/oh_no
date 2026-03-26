package com.easyaccounting.util

import com.easyaccounting.R
import java.util.Locale

object IconUtils {
    fun getIconResourceId(iconName: String?): Int = when (normalize(iconName)) {
        "ic_food", "food", "餐饮" -> R.drawable.ic_food
        "ic_transport", "transport", "交通" -> R.drawable.ic_transport
        "ic_shopping", "shopping", "购物" -> R.drawable.ic_shopping
        "ic_entertainment", "entertainment", "娱乐" -> R.drawable.ic_entertainment
        "ic_medical", "medical", "医疗" -> R.drawable.ic_medical
        "ic_education", "education", "教育" -> R.drawable.ic_education
        "ic_salary", "salary", "工资" -> R.drawable.ic_salary
        "ic_bonus", "bonus", "奖金" -> R.drawable.ic_bonus
        "ic_investment", "investment", "投资", "投资收益" -> R.drawable.ic_investment
        "ic_alipay", "alipay", "支付宝" -> R.drawable.ic_alipay
        "ic_wechat", "wechat", "微信" -> R.drawable.ic_wechat
        "ic_meituan", "meituan", "美团" -> R.drawable.ic_meituan
        "ic_douyin", "douyin", "抖音" -> R.drawable.ic_douyin
        "ic_jd", "jd", "京东" -> R.drawable.ic_jd
        "ic_bank", "bank", "bank_card", "银行卡" -> R.drawable.ic_bank
        "ic_cash", "cash", "现金" -> R.drawable.ic_cash
        "ic_other_source" -> R.drawable.ic_other_source
        else -> R.drawable.ic_other
    }

    fun getAccountIconResourceId(accountType: String?): Int = when (normalize(accountType)) {
        "alipay" -> R.drawable.ic_alipay
        "wechat" -> R.drawable.ic_wechat
        "bank_card", "bank" -> R.drawable.ic_bank
        "cash" -> R.drawable.ic_cash
        else -> R.drawable.ic_other_source
    }

    fun getCategoryIconByName(name: String?): Int = when (normalize(name)) {
        "餐饮", "food" -> R.drawable.ic_food
        "交通", "transport" -> R.drawable.ic_transport
        "购物", "shopping" -> R.drawable.ic_shopping
        "娱乐", "entertainment" -> R.drawable.ic_entertainment
        "医疗", "medical" -> R.drawable.ic_medical
        "教育", "education" -> R.drawable.ic_education
        "工资", "salary" -> R.drawable.ic_salary
        "奖金", "bonus" -> R.drawable.ic_bonus
        "投资收益", "投资", "investment" -> R.drawable.ic_investment
        else -> R.drawable.ic_other
    }

    fun getIncomeSourceIconByName(source: String?): Int = when (normalize(source)) {
        "工资", "salary" -> R.drawable.ic_salary
        "奖金", "bonus" -> R.drawable.ic_bonus
        "投资收益", "投资", "investment" -> R.drawable.ic_investment
        "支付宝", "alipay" -> R.drawable.ic_alipay
        "微信", "wechat" -> R.drawable.ic_wechat
        "美团", "meituan" -> R.drawable.ic_meituan
        "抖音", "douyin" -> R.drawable.ic_douyin
        "京东", "jd" -> R.drawable.ic_jd
        "银行卡", "bank", "bank_card" -> R.drawable.ic_bank
        "现金", "cash" -> R.drawable.ic_cash
        "其他", "other" -> R.drawable.ic_other_source
        else -> getCategoryIconByName(source)
    }

    private fun normalize(value: String?): String {
        return value?.trim()?.lowercase(Locale.ROOT).orEmpty()
    }
}
