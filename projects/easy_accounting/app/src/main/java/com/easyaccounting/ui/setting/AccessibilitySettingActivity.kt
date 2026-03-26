package com.easyaccounting.ui.setting

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.easyaccounting.R
import com.easyaccounting.service.FloatBubbleService
import com.easyaccounting.service.PayAccessibilityService
import com.easyaccounting.util.AppPreferences
import com.easyaccounting.util.ThemeUtils

class AccessibilitySettingActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var switchAutoAccounting: SwitchCompat
    private lateinit var tvServiceStatus: TextView
    private lateinit var btnOpenAccessibility: Button
    private lateinit var btnOpenOverlay: Button
    private lateinit var ivAlipayStatus: ImageView
    private lateinit var ivWechatStatus: ImageView
    private lateinit var ivMeituanStatus: ImageView
    private lateinit var ivDouyinStatus: ImageView
    private lateinit var ivJdStatus: ImageView
    private lateinit var themeGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySelectedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accessibility_setting)
        ThemeUtils.applyScreenBackground(findViewById(R.id.accessibilityRoot), this)

        prefs = AppPreferences.prefs(this)

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
        ivMeituanStatus = findViewById(R.id.iv_meituan_status)
        ivDouyinStatus = findViewById(R.id.iv_douyin_status)
        ivJdStatus = findViewById(R.id.iv_jd_status)
        themeGroup = findViewById(R.id.rg_theme)

        switchAutoAccounting.isChecked = prefs.getBoolean(AppPreferences.KEY_AUTO_ACCOUNTING, true)
        themeGroup.check(ThemeUtils.getThemeRadioButtonId(AppPreferences.getSelectedThemeName(this)))
    }

    private fun setupListeners() {
        switchAutoAccounting.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(AppPreferences.KEY_AUTO_ACCOUNTING, isChecked).apply()
            updateAutoAccountingService(isChecked)
            updateServiceStatus()

            if (isChecked && !isAccessibilityServiceEnabled()) {
                Toast.makeText(this, "请先开启无障碍服务", Toast.LENGTH_SHORT).show()
            }
        }

        btnOpenAccessibility.setOnClickListener { openAccessibilitySettings() }
        btnOpenOverlay.setOnClickListener { openOverlaySettings() }

        themeGroup.setOnCheckedChangeListener { _, checkedId ->
            val theme = ThemeUtils.getThemeNameForRadioButton(checkedId) ?: return@setOnCheckedChangeListener
            if (theme == AppPreferences.getSelectedThemeName(this)) {
                return@setOnCheckedChangeListener
            }
            prefs.edit().putString(AppPreferences.KEY_THEME, theme).apply()
            Toast.makeText(this, "主题已切换", Toast.LENGTH_SHORT).show()
            recreate()
        }
    }

    private fun updateServiceStatus() {
        val autoEnabled = prefs.getBoolean(AppPreferences.KEY_AUTO_ACCOUNTING, true)
        val accessibilityEnabled = isAccessibilityServiceEnabled()
        val overlayEnabled = isOverlayEnabled()

        tvServiceStatus.text = when {
            !autoEnabled -> "自动获取已关闭"
            accessibilityEnabled && overlayEnabled -> "服务已就绪"
            accessibilityEnabled -> "请开启悬浮窗权限"
            else -> "服务未启用"
        }

        val iconVisible = autoEnabled && accessibilityEnabled
        val visibility = if (iconVisible) View.VISIBLE else View.GONE
        ivAlipayStatus.visibility = visibility
        ivWechatStatus.visibility = visibility
        ivMeituanStatus.visibility = visibility
        ivDouyinStatus.visibility = visibility
        ivJdStatus.visibility = visibility

        btnOpenAccessibility.text = if (accessibilityEnabled) "无障碍服务已开启" else "开启无障碍服务"
        btnOpenOverlay.text = if (overlayEnabled) "悬浮窗权限已开启" else "开启悬浮窗权限"
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )

        val targetPackage = packageName
        val targetClass = PayAccessibilityService::class.java.name

        return enabledServices.any { service ->
            service.resolveInfo?.serviceInfo?.let { serviceInfo ->
                serviceInfo.packageName == targetPackage && serviceInfo.name == targetClass
            } ?: false
        }
    }

    private fun isOverlayEnabled(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    private fun openAccessibilitySettings() {
        try {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        } catch (_: Exception) {
            try {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.parse("package:$packageName")
                })
            } catch (_: Exception) {
                Toast.makeText(this, "请手动前往系统设置开启服务", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openOverlaySettings() {
        try {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:$packageName")
                )
            )
        } catch (_: Exception) {
            Toast.makeText(this, "请手动前往系统设置开启悬浮窗权限", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateAutoAccountingService(enabled: Boolean) {
        val bubbleIntent = Intent(this, FloatBubbleService::class.java).apply {
            action = if (enabled) {
                FloatBubbleService.ACTION_SHOW_BUBBLE
            } else {
                FloatBubbleService.ACTION_DISMISS_BUBBLE
            }
        }
        startService(bubbleIntent)
    }

    fun onBackPressed(view: View) {
        finish()
    }
}
