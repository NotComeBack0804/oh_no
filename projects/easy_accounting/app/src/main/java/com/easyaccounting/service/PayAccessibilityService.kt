package com.easyaccounting.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import com.easyaccounting.R
import com.easyaccounting.data.database.AppDatabase
import com.easyaccounting.data.entity.PaySource
import com.easyaccounting.data.entity.PendingRecord
import com.easyaccounting.data.entity.PendingStatus
import com.easyaccounting.ui.MainActivity
import com.easyaccounting.util.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import kotlin.math.abs

/**
 * 监听支付结果页并自动生成待确认账单。
 * 目前重点覆盖：支付宝、微信、美团、抖音、京东。
 */
class PayAccessibilityService : AccessibilityService() {

    companion object {
        const val ACTION_ENABLE_AUTO_ACCOUNTING = "com.easyaccounting.ENABLE_AUTO_ACCOUNTING"
        const val ACTION_DISABLE_AUTO_ACCOUNTING = "com.easyaccounting.DISABLE_AUTO_ACCOUNTING"

        const val NOTIFICATION_CHANNEL_ID = "easy_accounting_accessibility"
        const val NOTIFICATION_ID = 1001

        private const val TAG = "PayAccessibility"

        const val PACKAGE_ALIPAY = "com.eg.android.AlipayGPhone"
        const val PACKAGE_WECHAT = "com.tencent.mm"
        const val PACKAGE_MEITUAN = "com.sankuai.meituan"
        const val PACKAGE_DOUYIN = "com.ss.android.ugc.aweme"
        const val PACKAGE_JD = "com.jingdong.app.mall"

        private val AMOUNT_PATTERN = Pattern.compile(
            "(?:[¥￥]|人民币)?\\s*(\\d+(?:,\\d{3})*(?:\\.\\d{1,2})?)\\s*(?:元|块钱|块|rmb|RMB)?"
        )

        private val ALIPAY_OUTGOING_KEYWORDS = listOf(
            "支付成功", "支付完成", "已支付", "交易成功", "付款成功", "扫码成功", "付款详情", "实付"
        )
        private val ALIPAY_TRANSFER_KEYWORDS = listOf(
            "转账成功", "转账给", "转账详情", "转账说明", "待对方收款", "已转账"
        )

        private val WECHAT_PAYMENT_KEYWORDS = listOf(
            "支付成功", "付款成功", "微信支付", "交易成功", "商户单号", "付款金额", "支付金额", "实付"
        )
        private val WECHAT_TRANSFER_SCENE_KEYWORDS = listOf(
            "转账给朋友", "转账给", "微信转账", "转账说明", "收款方", "转账时间", "转账单号"
        )
        private val WECHAT_TRANSFER_RESULT_KEYWORDS = listOf(
            "已转账", "待收款", "朋友确认收款后", "转账到账", "转账成功"
        )
        private val WECHAT_INCOMING_KEYWORDS = listOf(
            "向你转账", "收款到账", "已收款", "你已收款", "已确认收款", "已存入零钱", "已存入银行卡"
        )

        private val MEITUAN_SUCCESS_KEYWORDS = listOf(
            "支付成功", "付款成功", "交易成功", "买单成功", "实付"
        )
        private val DOUYIN_SUCCESS_KEYWORDS = listOf(
            "支付成功", "付款成功", "交易成功", "抖音月付支付成功", "实付款"
        )
        private val JD_SUCCESS_KEYWORDS = listOf(
            "支付成功", "付款成功", "交易成功", "白条支付成功", "京东支付成功", "实付款"
        )

        private val POSITIVE_AMOUNT_CONTEXT = linkedMapOf(
            "实付" to 8,
            "实付款" to 8,
            "支付金额" to 7,
            "付款金额" to 7,
            "转账金额" to 7,
            "交易金额" to 6,
            "合计" to 5,
            "订单金额" to 5,
            "支付成功" to 5,
            "付款成功" to 5,
            "已转账" to 5,
            "待收款" to 4,
            "金额" to 2
        )
        private val NEGATIVE_AMOUNT_CONTEXT = linkedMapOf(
            "优惠" to -7,
            "立减" to -7,
            "折扣" to -6,
            "红包优惠" to -6,
            "返现" to -6,
            "余额" to -5,
            "剩余" to -5,
            "服务费" to -4,
            "手续费" to -4,
            "积分" to -4,
            "尾号" to -4,
            "订单号" to -8,
            "流水号" to -8,
            "商户单号" to -7,
            "单号" to -7
        )

        var isServiceRunning = false
            private set

        var autoAccountingEnabled = true
            private set
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var prefs: SharedPreferences

    private var lastProcessedFingerprint: String = ""
    private var lastProcessedTime: Long = 0
    private val debounceMillis = 2_000L

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == AppPreferences.KEY_AUTO_ACCOUNTING) {
            autoAccountingEnabled = prefs.getBoolean(AppPreferences.KEY_AUTO_ACCOUNTING, true)
            Log.d(TAG, "autoAccountingEnabled updated: $autoAccountingEnabled")
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = AppPreferences.prefs(this)
        autoAccountingEnabled = prefs.getBoolean(AppPreferences.KEY_AUTO_ACCOUNTING, true)
        prefs.registerOnSharedPreferenceChangeListener(prefsListener)
        isServiceRunning = false

        createNotificationChannel()
        startForegroundServiceWithNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_ENABLE_AUTO_ACCOUNTING -> {
                autoAccountingEnabled = true
                prefs.edit().putBoolean(AppPreferences.KEY_AUTO_ACCOUNTING, true).apply()
            }

            ACTION_DISABLE_AUTO_ACCOUNTING -> {
                autoAccountingEnabled = false
                prefs.edit().putBoolean(AppPreferences.KEY_AUTO_ACCOUNTING, false).apply()
            }
        }
        return START_STICKY
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!autoAccountingEnabled || event == null) return

        val packageName = event.packageName?.toString().orEmpty()
        val source = resolvePaySource(packageName) ?: return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> processEvent(source, event)
        }
    }

    override fun onInterrupt() = Unit

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        autoAccountingEnabled = prefs.getBoolean(AppPreferences.KEY_AUTO_ACCOUNTING, true)

        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
            packageNames = arrayOf(
                PACKAGE_ALIPAY,
                PACKAGE_WECHAT,
                PACKAGE_MEITUAN,
                PACKAGE_DOUYIN,
                PACKAGE_JD
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
        serviceScope.cancel()
    }

    private fun processEvent(source: PaySource, event: AccessibilityEvent) {
        val pageText = extractTextFromEvent(event)
        if (pageText.isBlank()) return

        if (!isPaymentPage(source, pageText)) return

        val amount = extractAmount(pageText) ?: return
        if (amount <= 0) return

        val fingerprint = "${source.name}|${amount}|${pageText.take(120)}"
        val now = System.currentTimeMillis()
        if (fingerprint == lastProcessedFingerprint && now - lastProcessedTime < debounceMillis) {
            return
        }

        lastProcessedFingerprint = fingerprint
        lastProcessedTime = now
        Log.d(TAG, "Matched payment page, source=$source, amount=$amount")

        serviceScope.launch {
            savePendingRecord(
                PendingRecord(
                    amount = amount,
                    source = source,
                    date = now,
                    status = PendingStatus.PENDING
                )
            )
        }
    }

    private fun resolvePaySource(packageName: String): PaySource? {
        return when {
            matchesPackage(packageName, PACKAGE_ALIPAY) -> PaySource.ALIPAY
            matchesPackage(packageName, PACKAGE_WECHAT) -> PaySource.WECHAT
            matchesPackage(packageName, PACKAGE_MEITUAN) -> PaySource.MEITUAN
            matchesPackage(packageName, PACKAGE_DOUYIN) -> PaySource.DOUYIN
            matchesPackage(packageName, PACKAGE_JD) -> PaySource.JD
            else -> null
        }
    }

    private fun matchesPackage(packageName: String, expectedPackage: String): Boolean {
        return packageName == expectedPackage || packageName.startsWith("$expectedPackage:")
    }

    private fun isPaymentPage(source: PaySource, text: String): Boolean {
        val matched = when (source) {
            PaySource.ALIPAY -> {
                containsAny(text, ALIPAY_OUTGOING_KEYWORDS) || containsAny(text, ALIPAY_TRANSFER_KEYWORDS)
            }

            PaySource.WECHAT -> {
                val outgoingPayment = containsAny(text, WECHAT_PAYMENT_KEYWORDS)
                val outgoingTransfer = containsAny(text, WECHAT_TRANSFER_SCENE_KEYWORDS) &&
                    containsAny(text, WECHAT_TRANSFER_RESULT_KEYWORDS)
                val incomingTransfer = containsAny(text, WECHAT_INCOMING_KEYWORDS)
                (outgoingPayment || outgoingTransfer) && !incomingTransfer
            }

            PaySource.MEITUAN -> containsAny(text, MEITUAN_SUCCESS_KEYWORDS)
            PaySource.DOUYIN -> containsAny(text, DOUYIN_SUCCESS_KEYWORDS)
            PaySource.JD -> containsAny(text, JD_SUCCESS_KEYWORDS)
            PaySource.OTHER -> false
        }

        return matched && extractAmount(text) != null
    }

    private fun containsAny(text: String, keywords: List<String>): Boolean {
        return keywords.any { keyword -> text.contains(keyword, ignoreCase = true) }
    }

    private fun extractAmount(text: String): Double? {
        val matcher = AMOUNT_PATTERN.matcher(text)
        val scoredAmounts = mutableListOf<Pair<Double, Int>>()
        val fallbackAmounts = mutableListOf<Double>()

        while (matcher.find()) {
            val amountStr = matcher.group(1) ?: continue
            val amount = amountStr.replace(",", "").toDoubleOrNull() ?: continue
            if (amount <= 0 || amount >= 1_000_000) continue

            val contextStart = (matcher.start() - 14).coerceAtLeast(0)
            val contextEnd = (matcher.end() + 14).coerceAtMost(text.length)
            val context = text.substring(contextStart, contextEnd)
            val score = scoreAmountContext(context)

            val looksLikeDateOrId = amount >= 1_000 &&
                amount % 1.0 == 0.0 &&
                score <= 0 &&
                !context.contains("¥") &&
                !context.contains("￥") &&
                !context.contains("元") &&
                !context.contains("块")

            if (!looksLikeDateOrId) {
                fallbackAmounts += amount
                scoredAmounts += amount to score
            }
        }

        val positiveMatches = scoredAmounts.filter { it.second > 0 }
        if (positiveMatches.isNotEmpty()) {
            return positiveMatches
                .sortedWith(compareByDescending<Pair<Double, Int>> { it.second }.thenByDescending { it.first })
                .first()
                .first
        }

        val uniqueAmounts = fallbackAmounts.distinct().sorted()
        if (uniqueAmounts.isEmpty()) return null
        return if (uniqueAmounts.size == 1) uniqueAmounts.first() else uniqueAmounts.last()
    }

    private fun scoreAmountContext(context: String): Int {
        var score = 0
        POSITIVE_AMOUNT_CONTEXT.forEach { (keyword, weight) ->
            if (context.contains(keyword)) score += weight
        }
        NEGATIVE_AMOUNT_CONTEXT.forEach { (keyword, weight) ->
            if (context.contains(keyword)) score += weight
        }
        if (context.contains("¥") || context.contains("￥") || context.contains("元") || context.contains("块")) {
            score += 2
        }
        return score
    }

    private fun extractTextFromEvent(event: AccessibilityEvent): String {
        val builder = StringBuilder()

        event.text?.forEach { item ->
            if (!item.isNullOrBlank()) {
                builder.append(item).append(' ')
            }
        }

        event.contentDescription?.let { description ->
            if (description.isNotBlank()) {
                builder.append(description).append(' ')
            }
        }

        val rootNode = rootInActiveWindow ?: event.source
        if (rootNode != null) {
            try {
                appendAllTextFromNode(rootNode, builder)
            } finally {
                rootNode.recycle()
            }
        }

        return builder.toString()
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun appendAllTextFromNode(node: AccessibilityNodeInfo?, builder: StringBuilder) {
        if (node == null) return

        node.text?.toString()
            ?.takeIf { it.isNotBlank() }
            ?.let { builder.append(it).append(' ') }

        node.contentDescription?.toString()
            ?.takeIf { it.isNotBlank() }
            ?.let { builder.append(it).append(' ') }

        for (index in 0 until node.childCount) {
            val child = node.getChild(index) ?: continue
            try {
                appendAllTextFromNode(child, builder)
            } finally {
                child.recycle()
            }
        }
    }

    private suspend fun savePendingRecord(pendingRecord: PendingRecord) {
        runCatching {
            val database = AppDatabase.getInstance(this)
            val latestRecord = database.pendingRecordDao().getLatestPendingRecord(PendingStatus.PENDING)
            val isDuplicate = latestRecord != null &&
                latestRecord.source == pendingRecord.source &&
                abs(latestRecord.amount - pendingRecord.amount) < 0.01 &&
                abs(latestRecord.date - pendingRecord.date) < 120_000

            if (isDuplicate) {
                Log.d(TAG, "Skip duplicate pending record: ${pendingRecord.source} ${pendingRecord.amount}")
                return
            }

            database.pendingRecordDao().insert(pendingRecord)
            showFloatBubble()
        }.onFailure { error ->
            Log.e(TAG, "savePendingRecord failed", error)
        }
    }

    private fun showFloatBubble() {
        val intent = Intent(this, FloatBubbleService::class.java).apply {
            action = FloatBubbleService.ACTION_SHOW_BUBBLE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "自动记账服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持支付监听服务常驻运行"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceWithNotification() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("自动记账服务运行中")
            .setContentText("已开启支付监听页面监听，点击返回应用")
            .setSmallIcon(R.drawable.ic_auto_accounting)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }
}
