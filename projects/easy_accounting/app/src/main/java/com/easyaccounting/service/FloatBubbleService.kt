package com.easyaccounting.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
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
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.easyaccounting.R
import com.easyaccounting.data.database.AppDatabase
import com.easyaccounting.data.entity.Bill
import com.easyaccounting.data.entity.CategoryType
import com.easyaccounting.data.entity.PendingRecord
import com.easyaccounting.data.entity.PendingStatus
import com.easyaccounting.ui.MainActivity
import com.easyaccounting.util.AutoAccountingSourceUtils
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

class FloatBubbleService : Service() {

    companion object {
        const val ACTION_SHOW_BUBBLE = "com.easyaccounting.SHOW_BUBBLE"
        const val ACTION_DISMISS_BUBBLE = "com.easyaccounting.DISMISS_BUBBLE"
        const val ACTION_CONFIRM_BILL = "com.easyaccounting.CONFIRM_BILL"
        const val ACTION_IGNORE_BILL = "com.easyaccounting.IGNORE_BILL"

        private const val CHANNEL_ID = "easy_accounting_float_bubble"
        private const val NOTIFICATION_ID = 10001
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var windowManager: WindowManager? = null
    private var bubbleView: View? = null
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
                val recordId = intent.getLongExtra("record_id", -1L)
                val categoryId = intent.getLongExtra("category_id", -1L).takeIf { it > 0 }
                if (recordId > 0) {
                    confirmBill(recordId, categoryId)
                }
            }

            ACTION_IGNORE_BILL -> {
                val recordId = intent.getLongExtra("record_id", -1L)
                if (recordId > 0) {
                    ignoreBill(recordId)
                }
            }
        }
        return START_STICKY
    }

    private fun showBubble() {
        dismissBubble()

        collectJob?.cancel()
        collectJob = serviceScope.launch {
            val database = AppDatabase.getInstance(this@FloatBubbleService)
            database.pendingRecordDao()
                .getPendingRecordsByStatus(PendingStatus.PENDING)
                .collectLatest { records ->
                    val record = records.firstOrNull()
                    if (record == null) {
                        dismissBubble()
                    } else {
                        showBubbleWindow(record)
                    }
                }
        }
    }

    @SuppressLint("InflateParams")
    private fun showBubbleWindow(record: PendingRecord) {
        if (bubbleView != null) {
            updateBubbleContent(record)
            return
        }

        bubbleView = LayoutInflater.from(this).inflate(R.layout.layout_float_bubble, null)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 16
            y = 200
        }

        bubbleView?.let { view ->
            setupBubbleView(view, record)
            setupTouchListener(view, params)
            try {
                windowManager?.addView(view, params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener(view: View, params: WindowManager.LayoutParams) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var moved = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    moved = false
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (dx * dx + dy * dy > 100) {
                        moved = true
                    }
                    params.x = initialX + dx
                    params.y = initialY + dy
                    windowManager?.updateViewLayout(view, params)
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (!moved) {
                        view.performClick()
                    }
                    true
                }

                else -> false
            }
        }
    }

    private fun setupBubbleView(view: View, record: PendingRecord) {
        updateBubbleContent(record)

        val btnFood = view.findViewById<ImageButton>(R.id.btn_category_food)
        val btnTransport = view.findViewById<ImageButton>(R.id.btn_category_transport)
        val btnShopping = view.findViewById<ImageButton>(R.id.btn_category_shopping)
        val btnOther = view.findViewById<ImageButton>(R.id.btn_category_other)
        val btnConfirm = view.findViewById<ImageButton>(R.id.btn_confirm)
        val btnIgnore = view.findViewById<ImageButton>(R.id.btn_ignore)

        serviceScope.launch {
            val database = AppDatabase.getInstance(this@FloatBubbleService)
            val foodCategory = database.categoryDao().getCategoryByNameAndType("餐饮", CategoryType.EXPENSE)
            val transportCategory = database.categoryDao().getCategoryByNameAndType("交通", CategoryType.EXPENSE)
            val shoppingCategory = database.categoryDao().getCategoryByNameAndType("购物", CategoryType.EXPENSE)
            val otherCategory = database.categoryDao().getCategoryByNameAndType("其他", CategoryType.EXPENSE)

            btnFood.setOnClickListener { foodCategory?.id?.let { id -> confirmBill(record.id, id) } }
            btnTransport.setOnClickListener { transportCategory?.id?.let { id -> confirmBill(record.id, id) } }
            btnShopping.setOnClickListener { shoppingCategory?.id?.let { id -> confirmBill(record.id, id) } }
            btnOther.setOnClickListener { otherCategory?.id?.let { id -> confirmBill(record.id, id) } }
        }

        btnConfirm.setOnClickListener { confirmBill(record.id, null) }
        btnIgnore.setOnClickListener { ignoreBill(record.id) }
    }

    @SuppressLint("SetTextI18n")
    private fun updateBubbleContent(record: PendingRecord) {
        bubbleView?.let { view ->
            val tvAmount = view.findViewById<TextView>(R.id.tv_bubble_amount)
            val tvSource = view.findViewById<TextView>(R.id.tv_bubble_source)
            val tvTime = view.findViewById<TextView>(R.id.tv_bubble_time)
            val ivSourceIcon = view.findViewById<ImageView>(R.id.iv_source_icon)

            tvAmount.text = "¥${String.format(Locale.getDefault(), "%.2f", record.amount)}"
            tvSource.text = AutoAccountingSourceUtils.getSourceText(record.source)
            ivSourceIcon.setImageResource(AutoAccountingSourceUtils.getSourceIcon(record.source))

            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            tvTime.text = dateFormat.format(Date(record.date))
        }
    }

    private fun confirmBill(recordId: Long, categoryId: Long?) {
        serviceScope.launch {
            try {
                val database = AppDatabase.getInstance(this@FloatBubbleService)
                val record = database.pendingRecordDao().getPendingRecordById(recordId) ?: return@launch

                val accountName = AutoAccountingSourceUtils.getSourceText(record.source)
                val account = database.accountDao().getAccountByName(accountName)
                val resolvedCategoryId = categoryId
                    ?: database.categoryDao().getCategoryByNameAndType("其他", CategoryType.EXPENSE)?.id

                val bill = Bill(
                    amount = record.amount,
                    categoryId = resolvedCategoryId,
                    date = record.date,
                    remark = AutoAccountingSourceUtils.buildAutoRemark(record.source),
                    accountId = account?.id,
                    createdAt = System.currentTimeMillis()
                )
                database.billDao().insert(bill)
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
        bubbleView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        bubbleView = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "自动记账服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "用于显示待确认账单悬浮窗"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
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
            .setSmallIcon(R.drawable.ic_auto_accounting)
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
