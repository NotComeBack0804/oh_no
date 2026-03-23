package com.easyaccounting.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.preference.PreferenceManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.easyaccounting.data.database.AppDatabase
import com.easyaccounting.data.entity.PaySource
import com.easyaccounting.data.entity.PendingRecord
import com.easyaccounting.service.FloatBubbleService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * 支付监听无障碍服务
 * 监听支付宝和微信的支付成功页面，自动提取金额并创建待确认账单
 *
 * 隐私声明：
 * 本服务仅在用户主动开启后生效，仅读取屏幕上的支付金额信息，
 * 不会收集、存储或上传任何个人信息。数据仅保存在本地设备中。
 */
class PayAccessibilityService : AccessibilityService() {

    companion object {
        const val ACTION_ENABLE_AUTO_ACCOUNTING = "com.easyaccounting.ENABLE_AUTO_ACCOUNTING"
        const val ACTION_DISABLE_AUTO_ACCOUNTING = "com.easyaccounting.DISABLE_AUTO_ACCOUNTING"

        // 支付宝包名
        const val PACKAGE_ALIPAY = "com.eg.android.AlipayGPhone"
        // 微信包名
        const val PACKAGE_WECHAT = "com.tencent.mm"

        // 金额正则：匹配 ¥123.45 或 123.45 格式
        private val AMOUNT_PATTERN = Pattern.compile("¥?(\\d+\\.?\\d{0,2})")

        // 支付宝支付成功关键词
        private val ALIPAY_SUCCESS_KEYWORDS = listOf(
            "支付成功", "支付完成", "已支付", "交易成功"
        )
        // 微信支付成功关键词
        private val WECHAT_SUCCESS_KEYWORDS = listOf(
            "支付成功", "微信支付", "交易成功", "收款到账"
        )

        var isServiceRunning = false
            private set

        // 全局通知开关（用户可在设置中关闭）
        var autoAccountingEnabled = true
            private set
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var prefs: SharedPreferences
    private var lastProcessedText: String = ""
    private var lastProcessedTime: Long = 0
    private val debounceMillis = 3000L // 3秒防抖

    override fun onCreate() {
        super.onCreate()
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        autoAccountingEnabled = prefs.getBoolean("auto_accounting_enabled", true)
        isServiceRunning = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_ENABLE_AUTO_ACCOUNTING -> {
                autoAccountingEnabled = true
                prefs.edit().putBoolean("auto_accounting_enabled", true).apply()
            }
            ACTION_DISABLE_AUTO_ACCOUNTING -> {
                autoAccountingEnabled = false
                prefs.edit().putBoolean("auto_accounting_enabled", false).apply()
            }
        }
        return START_STICKY
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!autoAccountingEnabled) return
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return

        // 仅处理支付宝和微信
        if (packageName != PACKAGE_ALIPAY && packageName != PACKAGE_WECHAT) return

        // 防抖处理
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastProcessedTime < debounceMillis) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowStateChanged(event, packageName)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // 仅在关键页面时处理内容变化
                handleWindowContentChanged(event, packageName)
            }
        }
    }

    private fun handleWindowStateChanged(event: AccessibilityEvent, packageName: String) {
        val className = event.className?.toString() ?: return
        val text = extractText(event)

        val keywords = if (packageName == PACKAGE_ALIPAY) ALIPAY_SUCCESS_KEYWORDS else WECHAT_SUCCESS_KEYWORDS
        val isPaymentPage = keywords.any { text.contains(it) }

        if (isPaymentPage) {
            lastProcessedTime = System.currentTimeMillis()
            lastProcessedText = text
            extractAndSavePayment(packageName, text)
        }
    }

    private fun handleWindowContentChanged(event: AccessibilityEvent, packageName: String) {
        val text = extractText(event)
        if (text == lastProcessedText || text.isEmpty()) return

        val keywords = if (packageName == PACKAGE_ALIPAY) ALIPAY_SUCCESS_KEYWORDS else WECHAT_SUCCESS_KEYWORDS
        val isPaymentPage = keywords.any { text.contains(it) }

        if (isPaymentPage) {
            lastProcessedTime = System.currentTimeMillis()
            lastProcessedText = text
            extractAndSavePayment(packageName, text)
        }
    }

    /**
     * 从页面文本中提取金额并保存为待确认账单
     */
    private fun extractAndSavePayment(packageName: String, pageText: String) {
        val amount = extractAmount(pageText) ?: return
        val source = if (packageName == PACKAGE_ALIPAY) PaySource.ALIPAY else PaySource.WECHAT

        val pendingRecord = PendingRecord(
            amount = amount,
            source = source,
            date = System.currentTimeMillis(),
            status = com.easyaccounting.data.entity.PendingStatus.PENDING
        )

        serviceScope.launch {
            savePendingRecord(pendingRecord)
        }
    }

    /**
     * 提取金额
     * 支持格式：¥123.45, 123.45, ¥123, 123
     */
    private fun extractAmount(text: String): Double? {
        // 查找所有匹配的金额
        val matcher = AMOUNT_PATTERN.matcher(text)
        val amounts = mutableListOf<Double>()

        while (matcher.find()) {
            val amountStr = matcher.group(1) ?: continue
            val amount = amountStr.toDoubleOrNull() ?: continue
            // 过滤异常金额（太小或太大的可能是误匹配）
            if (amount > 0 && amount < 1000000) {
                amounts.add(amount)
            }
        }

        // 优先返回中等大小的金额（支付金额通常不会是最大的也不是最小的）
        if (amounts.isEmpty()) return null
        if (amounts.size == 1) return amounts[0]

        // 按金额大小排序，取中间值（避免选到误匹配的极小/极大值）
        amounts.sort()
        return amounts[amounts.size / 2]
    }

    /**
     * 从 AccessibilityEvent 中提取所有文本
     */
    private fun extractText(event: AccessibilityEvent): String {
        val sb = StringBuilder()
        val textList = event.text
        if (textList != null) {
            for (i in 0 until textList.size) {
                textList[i]?.let { sb.append(it).append(" ") }
            }
        }
        val contentDescription = event.contentDescription
        if (contentDescription != null) {
            sb.append(contentDescription).append(" ")
        }
        return sb.toString()
    }

    /**
     * 递归遍历节点树查找金额
     */
    private fun findAmountInNode(node: AccessibilityNodeInfo?): Double? {
        if (node == null) return null

        val text = node.text?.toString() ?: node.contentDescription?.toString() ?: ""
        val amount = extractAmount(text)
        if (amount != null) {
            return amount
        }

        // 遍历子节点
        for (i in 0 until node.childCount) {
            val childAmount = findAmountInNode(node.getChild(i))
            if (childAmount != null) {
                return childAmount
            }
        }
        return null
    }

    /**
     * 保存待确认账单到数据库
     */
    private suspend fun savePendingRecord(pendingRecord: PendingRecord) {
        try {
            val database = AppDatabase.getInstance(this)
            database.pendingRecordDao().insert(pendingRecord)

            // 通知悬浮窗服务显示气泡
            showFloatBubble()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 启动悬浮窗服务显示气泡
     */
    private fun showFloatBubble() {
        val intent = Intent(this, FloatBubbleService::class.java).apply {
            action = FloatBubbleService.ACTION_SHOW_BUBBLE
        }
        startService(intent)
    }

    override fun onInterrupt() {
        // 服务中断时不做处理
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
            packageNames = arrayOf(PACKAGE_ALIPAY, PACKAGE_WECHAT)
        }
        serviceInfo = info
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        serviceScope.cancel()
    }
}
