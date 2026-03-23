package com.easyaccounting.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy年MM月", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("MM-dd", Locale.getDefault())

    /**
     * 获取指定日期的开始时间戳（00:00:00）
     */
    fun getStartOfDay(timeMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * 获取指定日期的结束时间戳（23:59:59）
     */
    fun getEndOfDay(timeMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    /**
     * 获取指定月份的开始时间戳
     */
    fun getStartOfMonth(year: Int, month: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * 获取指定月份的结束时间戳
     */
    fun getEndOfMonth(year: Int, month: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1, 23, 59, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        return calendar.timeInMillis
    }

    /**
     * 获取今天的开始时间戳
     */
    fun getTodayStart(): Long = getStartOfDay(System.currentTimeMillis())

    /**
     * 获取今天的结束时间戳
     */
    fun getTodayEnd(): Long = getEndOfDay(System.currentTimeMillis())

    /**
     * 格式化日期
     */
    fun formatDate(timeMillis: Long): String = dateFormat.format(Date(timeMillis))

    /**
     * 格式化月份
     */
    fun formatMonth(year: Int, month: Int): String = monthFormat.format(
        Calendar.getInstance().apply {
            set(year, month - 1, 1)
        }.time
    )

    /**
     * 格式化日期（MM-dd）
     */
    fun formatDay(timeMillis: Long): String = dayFormat.format(Date(timeMillis))

    /**
     * 获取当前年份
     */
    fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

    /**
     * 获取当前月份
     */
    fun getCurrentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1

    /**
     * 获取月份的开始时间戳
     */
    fun getStartOfMonth(timeMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMillis
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * 获取N个月前的开始时间戳
     */
    fun getMonthsAgo(months: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -months)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
