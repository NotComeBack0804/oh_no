package com.easyaccounting.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.easyaccounting.EasyAccountingApp
import com.easyaccounting.R
import com.easyaccounting.data.database.AppDatabase
import com.easyaccounting.data.entity.Bill
import com.easyaccounting.data.entity.CategoryType
import com.easyaccounting.data.entity.PaySource
import com.easyaccounting.data.entity.PendingRecord
import com.easyaccounting.data.entity.PendingStatus
import com.easyaccounting.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 悬浮窗服务 - 显示待确认账单气泡
 * 使用 ForegroundService 保证后台运行
 */
class FloatBubbleService : Service() {

    companion object {
        const val ACTION_SHOW_BUBBLE = "com.easyaccounting.SHOW_BUBBLE"
        const val ACTION_DISMISS_BUBBLE = "com.easyaccounting.DISMISS_BUBBLE"
        const val ACTION_CONFIRM_BILL = "com.easyaccounting.CONFIRM_BILL"
        const val ACTION_IGNORE_BILL = "com.easyaccounting.IGNORE_BILL"

        private const val CHANNEL_ID = "easy_accounting_float_bubble"
        private const val NOTIFICATION_ID = 10001

        // 默认分类ID（未选择分类时的兜底）
        private const val DEFAULT_CATEGORY_ID = 7L // "其他"分类
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var windowManager: WindowManager? = null
    private var bubbleView: View? = null
    private var currentPendingRecord: PendingRecord? = null
    private var collectJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_BUBBLE -> showBubble()
            ACTION_DISMISS_BUBBLE -> dismissBubble()
            ACTION_CONFIRM_BILL -> {
                val recordId = intent.getLongExtra("record_id", -1)
                val categoryId = intent.getLongExtra("category_id", DEFAULT_CATEGORY_ID)
                if (recordId > 0) confirmBill(recordId, categoryId)
            }
            ACTION_IGNORE_BILL -> {
                val recordId = intent.getLongExtra("record_id", -1)
                if (recordId > 0) ignoreBill(recordId)
            }
        }
        return START_STICKY
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    private fun showBubble() {
        // 如果已有气泡，先移除
        dismissBubble()

        // 从数据库获取最新待确认记录
        collectJob?.cancel()
        collectJob = serviceScope.launch {
            val database = AppDatabase.getInstance(this@FloatBubbleService)
            database.pendingRecordDao().getPendingRecordsByStatus(PendingStatus.PENDING)
                .collectLatest { records ->
                    if (records.isNotEmpty()) {
                        currentPendingRecord = records.first()
                        currentPendingRecord?.let { showBubbleWindow(it) }
                    } else {
                        dismissBubble()
                    }
                }
        }
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    private fun showBubbleWindow(record: PendingRecord) {
        if (bubbleView != null) {
            updateBubbleContent(record)
            return
        }

        bubbleView = LayoutInflater.from(this).inflate(R.layout.layout_float_bubble, null)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 16
            y = 200
        }

        setupBubbleView(bubbleView!!, record)
        setupTouchListener(bubbleView!!, params)

        try {
            windowManager?.addView(bubbleView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener(view: View, params: WindowManager.LayoutParams) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isMoved = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isMoved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (dx * dx + dy * dy > 100) isMoved = true
                    params.x = initialX + dx
                    params.y = initialY + dy
                    windowManager?.updateViewLayout(view, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isMoved) {
                        // 点击展开/收起
                        view.performClick()
                    }
                    true
                }
                else -> false
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupBubbleView(view: View, record: PendingRecord) {
        val tvAmount = view.findViewById<TextView>(R.id.tv_bubble_amount)
        val tvSource = view.findViewById<TextView>(R.id.tv_bubble_source)
        val tvTime = view.findViewById<TextView>(R.id.tv_bubble_time)
        val btnFood = view.findViewById<ImageButton>(R.id.btn_category_food)
        val btnTransport = view.findViewById<ImageButton>(R.id.btn_category_transport)
        val btnShopping = view.findViewById<ImageButton>(R.id.btn_category_shopping)
        val btnOther = view.findViewById<ImageButton>(R.id.btn_category_other)
        val btnConfirm = view.findViewById<ImageButton>(R.id.btn_confirm)
        val btnIgnore = view.findViewById<ImageButton>(R.id.btn_ignore)

        // 显示金额
        tvAmount.text = "¥${String.format("%.2f", record.amount)}"

        // 显示来源
        tvSource.text = if (record.source == PaySource.ALIPAY) "支付宝" else "微信"
        tvSource.setCompoundDrawablesWithIntrinsicBounds(
            if (record.source == PaySource.ALIPAY) R.drawable.ic_alipay else R.drawable.ic_wechat,
            0, 0, 0
        )

        // 显示时间
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        tvTime.text = dateFormat.format(Date(record.date))

        // 分类快捷按钮点击 - 通过名称查询分类ID
        serviceScope.launch {
            val database = AppDatabase.getInstance(this@FloatBubbleService)
            val foodCat = database.categoryDao().getCategoryByNameAndType("餐饮", CategoryType.EXPENSE)
            val transportCat = database.categoryDao().getCategoryByNameAndType("交通", CategoryType.EXPENSE)
            val shoppingCat = database.categoryDao().getCategoryByNameAndType("购物", CategoryType.EXPENSE)
            val otherCat = database.categoryDao().getCategoryByNameAndType("其他", CategoryType.EXPENSE)

            btnFood.setOnClickListener { foodCat?.id?.let { id -> confirmWithCategory(record, id) } }
            btnTransport.setOnClickListener { transportCat?.id?.let { id -> confirmWithCategory(record, id) } }
            btnShopping.setOnClickListener { shoppingCat?.id?.let { id -> confirmWithCategory(record, id) } }
            btnOther.setOnClickListener { otherCat?.id?.let { id -> confirmWithCategory(record, id) } }
        }

        // 确认按钮（使用默认分类）
        btnConfirm.setOnClickListener { confirmBill(record.id, DEFAULT_CATEGORY_ID) }

        // 忽略按钮
        btnIgnore.setOnClickListener { ignoreBill(record.id) }
    }

    @SuppressLint("SetTextI18n")
    private fun updateBubbleContent(record: PendingRecord) {
        bubbleView?.let { view ->
            val tvAmount = view.findViewById<TextView>(R.id.tv_bubble_amount)
            val tvSource = view.findViewById<TextView>(R.id.tv_bubble_source)
            val tvTime = view.findViewById<TextView>(R.id.tv_bubble_time)

            tvAmount.text = "¥${String.format("%.2f", record.amount)}"
            tvSource.text = if (record.source == PaySource.ALIPAY) "支付宝" else "微信"
            tvSource.setCompoundDrawablesWithIntrinsicBounds(
                if (record.source == PaySource.ALIPAY) R.drawable.ic_alipay else R.drawable.ic_wechat,
                0, 0, 0
            )
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvTime.text = dateFormat.format(Date(record.date))
        }
    }

    private fun confirmWithCategory(record: PendingRecord, categoryId: Long) {
        confirmBill(record.id, categoryId)
    }

    private fun confirmBill(recordId: Long, categoryId: Long) {
        serviceScope.launch {
            try {
                val database = AppDatabase.getInstance(this@FloatBubbleService)
                val record = database.pendingRecordDao().getPendingRecordById(recordId) ?: return@launch

                // 根据支付来源查询对应的账户ID
                val accountName = if (record.source == PaySource.ALIPAY) "支付宝" else "微信"
                val account = database.accountDao().getAccountByName(accountName)
                val accountId = account?.id ?: 1L // 兜底默认账户

                // 创建正式账单
                val bill = Bill(
                    amount = record.amount,
                    categoryId = categoryId,
                    date = record.date,
                    remark = if (record.source == PaySource.ALIPAY) "支付宝自动记账" else "微信自动记账",
                    accountId = accountId,
                    createdAt = System.currentTimeMillis()
                )
                database.billDao().insert(bill)

                // 更新待确认记录状态
                database.pendingRecordDao().updateStatus(recordId, PendingStatus.CONFIRMED)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun ignoreBill(recordId: Long) {
        serviceScope.launch {
            try {
                val database = AppDatabase.getInstance(this@FloatBubbleService)
                database.pendingRecordDao().updateStatus(recordId, PendingStatus.IGNORED)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun dismissBubble() {
        bubbleView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            bubbleView = null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "自动记账服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "用于显示待确认账单悬浮气泡"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("简易记账")
            .setContentText("自动记账服务运行中，点击打开应用")
            .setSmallIcon(R.drawable.ic_alipay)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissBubble()
        collectJob?.cancel()
        serviceScope.cancel()
    }
}
