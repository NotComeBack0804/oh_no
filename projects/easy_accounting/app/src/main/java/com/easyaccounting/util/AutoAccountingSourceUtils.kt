package com.easyaccounting.util

import androidx.annotation.DrawableRes
import com.easyaccounting.R
import com.easyaccounting.data.entity.PaySource

object AutoAccountingSourceUtils {
    fun getSourceText(source: PaySource): String = when (source) {
        PaySource.ALIPAY -> "支付宝"
        PaySource.WECHAT -> "微信"
        PaySource.MEITUAN -> "美团"
        PaySource.DOUYIN -> "抖音"
        PaySource.JD -> "京东"
        PaySource.OTHER -> "其他"
    }

    @DrawableRes
    fun getSourceIcon(source: PaySource): Int = when (source) {
        PaySource.ALIPAY -> R.drawable.ic_alipay
        PaySource.WECHAT -> R.drawable.ic_wechat
        PaySource.MEITUAN -> R.drawable.ic_meituan
        PaySource.DOUYIN -> R.drawable.ic_douyin
        PaySource.JD -> R.drawable.ic_jd
        PaySource.OTHER -> R.drawable.ic_other_source
    }

    fun buildAutoRemark(source: PaySource): String = "${getSourceText(source)}自动记账"
}
