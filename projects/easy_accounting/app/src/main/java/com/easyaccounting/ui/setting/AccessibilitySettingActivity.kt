package com.easyaccounting.ui.setting

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.easyaccounting.R
import com.easyaccounting.service.FloatBubbleService
import com.easyaccounting.service.PayAccessibilityService

/**
 * 无障碍服务设置引导页面
 * 引导用户开启自动记账所需的无障碍服务和悬浮窗权限
 */
class AccessibilitySettingActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var switchAutoAccounting: SwitchCompat
    private lateinit var tvServiceStatus: TextView
    private lateinit var btnOpenAccessibility: Button
    private lateinit var btnOpenOverlay: Button
    private lateinit var ivAlipayStatus: ImageView
    private lateinit var ivWechatStatus: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accessibility_setting)

        prefs = getSharedPreferences("easy_accounting_prefs", Context.MODE_PRIVATE)

        initViews()
        setupListeners()
        updateServiceStatus()
    }

    override fun onResume() {
        super.onResume()
        updateServiceStatus()
    }

    private fun initViews() {
        switchAutoAccounting = findViewById(R.id.switch_auto_accounting)
        tvServiceStatus = findViewById(R.id.tv_service_status)
        btnOpenAccessibility = findViewById(R.id.btn_open_accessibility)
        btnOpenOverlay = findViewById(R.id.btn_open_overlay)
        ivAlipayStatus = findViewById(R.id.iv_alipay_status)
        ivWechatStatus = findViewById(R.id.iv_wechat_status)

        // 恢复开关状态
        switchAutoAccounting.isChecked = prefs.getBoolean("auto_accounting_enabled", true)
    }

    private fun setupListeners() {
        // 开关切换
        switchAutoAccounting.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_accounting_enabled", isChecked).apply()
            updateAutoAccountingService(isChecked)

            if (isChecked && !isAccessibilityServiceEnabled()) {
                Toast.makeText(this, "请先开启无障碍服务", Toast.LENGTH_SHORT).show()
            }
        }

        // 打开无障碍服务设置
        btnOpenAccessibility.setOnClickListener {
            openAccessibilitySettings()
        }

        // 打开悬浮窗权限设置
        btnOpenOverlay.setOnClickListener {
            openOverlaySettings()
        }
    }

    private fun updateServiceStatus() {
        val accessibilityEnabled = isAccessibilityServiceEnabled()
        val overlayEnabled = isOverlayEnabled()

        // 更新服务状态文本
        tvServiceStatus.text = when {
            accessibilityEnabled && overlayEnabled -> "✓ 服务已就绪"
            accessibilityEnabled && !overlayEnabled -> "⚠ 请开启悬浮窗权限"
            else -> "✗ 服务未启用"
        }

        // 更新图标状态
        ivAlipayStatus.visibility = if (accessibilityEnabled) View.VISIBLE else View.GONE
        ivWechatStatus.visibility = if (accessibilityEnabled) View.VISIBLE else View.GONE

        // 更新按钮文字
        btnOpenAccessibility.text = if (accessibilityEnabled) "无障碍服务已开启" else "开启无障碍服务"
        btnOpenOverlay.text = if (overlayEnabled) "悬浮窗权限已开启" else "开启悬浮窗权限"
    }

    /**
     * 检查无障碍服务是否已启用
     */
    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_GENERIC
        )

        val componentName = ComponentName(this, PayAccessibilityService::class.java)
        return enabledServices.any { it.resolveInfo.serviceInfo.componentName == componentName }
    }

    /**
     * 检查悬浮窗权限是否已授权
     */
    private fun isOverlayEnabled(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    /**
     * 打开无障碍服务设置页面
     */
    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            // 兜底：尝试直接打开应用详情页
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(this, "请手动在设置中开启", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * 打开悬浮窗权限设置页面
     */
    private fun openOverlaySettings() {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "请手动在设置中开启悬浮窗权限", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 启用/禁用自动记账相关服务
     */
    private fun updateAutoAccountingService(enabled: Boolean) {
        val serviceIntent = Intent(this, PayAccessibilityService::class.java).apply {
            action = if (enabled) {
                PayAccessibilityService.ACTION_ENABLE_AUTO_ACCOUNTING
            } else {
                PayAccessibilityService.ACTION_DISABLE_AUTO_ACCOUNTING
            }
        }
        startService(serviceIntent)

        // 同时控制悬浮窗服务
        val bubbleIntent = Intent(this, FloatBubbleService::class.java)
        if (enabled) {
            bubbleIntent.action = FloatBubbleService.ACTION_SHOW_BUBBLE
        } else {
            bubbleIntent.action = FloatBubbleService.ACTION_DISMISS_BUBBLE
        }
        startService(bubbleIntent)
    }

    /**
     * 返回按钮
     */
    fun onBackPressed(view: View) {
        finish()
    }
}
