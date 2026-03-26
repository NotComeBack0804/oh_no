package com.easyaccounting.ui.ai

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.easyaccounting.R
import com.easyaccounting.ai.AiProviderConfig
import com.easyaccounting.ai.AiProviderType
import com.easyaccounting.ai.AiSettingsStore
import com.easyaccounting.ai.BuiltinCompanionEngine
import com.easyaccounting.ai.CompanionPersonaPreset
import com.easyaccounting.ai.LlmGateway
import com.easyaccounting.data.entity.AiIntentType
import com.easyaccounting.databinding.ActivityAiSettingsBinding
import com.easyaccounting.util.ThemeUtils
import com.google.android.material.R as MaterialAttr
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.launch

class AiSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAiSettingsBinding
    private lateinit var settingsStore: AiSettingsStore

    private val llmGateway = LlmGateway()
    private val builtinEngine = BuiltinCompanionEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySelectedTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityAiSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ThemeUtils.applyScreenBackground(binding.settingsRoot, this)

        settingsStore = AiSettingsStore(applicationContext)

        setupToolbar()
        setupListeners()
        renderConfig(settingsStore.getConfig())
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "AI 设置"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() {
        binding.rgProvider.setOnCheckedChangeListener { _, checkedId ->
            updateProviderUi(providerFromCheckedId(checkedId))
            binding.tvTestResult.isVisible = false
        }

        binding.rgPersona.setOnCheckedChangeListener { _, checkedId ->
            val persona = personaFromCheckedId(checkedId)
            if (binding.etCompanionName.text.isNullOrBlank()) {
                binding.etCompanionName.setText(persona.defaultName)
            }
        }

        binding.btnTest.setOnClickListener { testCurrentConfig() }

        binding.btnSave.setOnClickListener {
            settingsStore.saveConfig(collectCurrentConfig())
            Toast.makeText(this, "AI 设置已保存", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun collectCurrentConfig(): AiProviderConfig {
        val persona = personaFromCheckedId(binding.rgPersona.checkedRadioButtonId)
        return AiProviderConfig(
            providerType = providerFromCheckedId(binding.rgProvider.checkedRadioButtonId),
            endpoint = binding.etEndpoint.text?.toString().orEmpty().trim(),
            model = binding.etModel.text?.toString().orEmpty().trim(),
            apiKey = binding.etApiKey.text?.toString().orEmpty().trim(),
            personaPreset = persona,
            companionName = binding.etCompanionName.text?.toString().orEmpty().trim()
                .ifBlank { persona.defaultName },
            customPrompt = binding.etCustomPrompt.text?.toString().orEmpty().trim()
        )
    }

    private fun testCurrentConfig() {
        val config = collectCurrentConfig()
        setTestingUi(testing = true)
        binding.tvTestResult.isVisible = true
        binding.tvTestResult.text = "正在测试 ${config.displayModelName()} ..."
        binding.tvTestResult.setTextColor(
            MaterialColors.getColor(binding.root, MaterialAttr.attr.colorOnSurfaceVariant)
        )

        lifecycleScope.launch {
            runCatching {
                when (config.providerType) {
                    AiProviderType.BUILTIN -> {
                        val sample = builtinEngine.reply(
                            input = "今天我在路上捡了100块钱",
                            config = config,
                            expenseCategories = listOf("餐饮", "交通", "购物", "娱乐", "医疗", "教育", "其他"),
                            incomeCategories = listOf("工资", "奖金", "投资收益", "其他")
                        )
                        val intentLabel = when (sample.intentType) {
                            AiIntentType.INCOME -> "收入"
                            AiIntentType.EXPENSE -> "支出"
                            AiIntentType.CLARIFY -> "追问"
                            AiIntentType.CHAT -> "闲聊"
                        }
                        "内置智脑可用：示例“今天我在路上捡了100块钱”识别为${intentLabel}，金额 ${sample.amount ?: 0.0}，分类 ${sample.category ?: "其他"}。"
                    }

                    else -> llmGateway.testConnection(config)
                }
            }.onSuccess { message ->
                showTestResult(
                    when (config.providerType) {
                        AiProviderType.BUILTIN -> message
                        else -> "${config.displayModelName()} 连接成功，可正常使用。"
                    },
                    success = true
                )
            }.onFailure { error ->
                showTestResult(
                    "${config.displayModelName()} 连接失败：${error.message ?: "请检查接口配置"}",
                    success = false
                )
            }

            setTestingUi(testing = false)
        }
    }

    private fun setTestingUi(testing: Boolean) {
        binding.btnTest.isEnabled = !testing
        binding.btnSave.isEnabled = !testing
        binding.progressTesting.isVisible = testing
    }

    private fun showTestResult(message: String, success: Boolean) {
        binding.tvTestResult.isVisible = true
        binding.tvTestResult.text = message
        binding.tvTestResult.setTextColor(
            MaterialColors.getColor(
                binding.root,
                if (success) MaterialAttr.attr.colorPrimary else MaterialAttr.attr.colorError
            )
        )
    }

    private fun renderConfig(config: AiProviderConfig) {
        binding.rgProvider.check(
            when (config.providerType) {
                AiProviderType.PRESET_GPT54 -> R.id.rb_provider_preset_gpt54
                AiProviderType.BUILTIN -> R.id.rb_provider_builtin
                AiProviderType.OPENAI -> R.id.rb_provider_openai
                AiProviderType.ANTHROPIC -> R.id.rb_provider_anthropic
                AiProviderType.COMPATIBLE -> R.id.rb_provider_compatible
            }
        )

        binding.etEndpoint.setText(config.endpoint)
        binding.etModel.setText(config.model)
        binding.etApiKey.setText(config.apiKey)
        binding.tvPresetModelValue.text = config.displayModelName()

        binding.rgPersona.check(
            when (config.personaPreset) {
                CompanionPersonaPreset.GENTLE_BOYFRIEND -> R.id.rb_persona_boyfriend
                CompanionPersonaPreset.LIVELY_GIRLFRIEND -> R.id.rb_persona_girlfriend
                CompanionPersonaPreset.COOL_BUTLER -> R.id.rb_persona_butler
                CompanionPersonaPreset.SAVAGE_FRIEND -> R.id.rb_persona_friend
            }
        )

        binding.etCompanionName.setText(config.resolvedCompanionName())
        binding.etCustomPrompt.setText(config.customPrompt)
        binding.tvTestResult.isVisible = false
        updateProviderUi(config.providerType)
    }

    private fun updateProviderUi(providerType: AiProviderType) {
        val showRemoteFields = providerType in setOf(
            AiProviderType.OPENAI,
            AiProviderType.ANTHROPIC,
            AiProviderType.COMPATIBLE
        )
        val showPresetFields = providerType == AiProviderType.PRESET_GPT54

        binding.groupRemoteFields.isVisible = showRemoteFields
        binding.groupPresetFields.isVisible = showPresetFields
        binding.tvPresetModelValue.text = providerType.displayName

        binding.tvProviderHint.text = when (providerType) {
            AiProviderType.PRESET_GPT54 ->
                "已封装内置官方通道，只展示模型名称。用户无需填写 Base URL、模型名或 API Key。"

            AiProviderType.BUILTIN ->
                "仅使用本地语义识别，不联网，不依赖 API。只有你手动选中它时才会生效。"

            AiProviderType.OPENAI ->
                "支持填写完整聊天接口，也支持填写根地址如 https://api.openai.com/v1，应用会自动补全 /chat/completions。"

            AiProviderType.ANTHROPIC ->
                "支持填写完整 Messages 接口，也支持填写根地址如 https://api.anthropic.com/v1，应用会自动补全 /messages。"

            AiProviderType.COMPATIBLE ->
                "适用于兼容 OpenAI 协议的平台。可填写完整接口，也可填写像 https://api.minimaxi.com/v1 这样的 Base URL，应用会自动补全 /chat/completions。"
        }

        if (showRemoteFields && binding.etEndpoint.text.isNullOrBlank()) {
            binding.etEndpoint.setText(providerType.defaultEndpoint)
        }
    }

    private fun providerFromCheckedId(checkedId: Int): AiProviderType {
        return when (checkedId) {
            R.id.rb_provider_preset_gpt54 -> AiProviderType.PRESET_GPT54
            R.id.rb_provider_openai -> AiProviderType.OPENAI
            R.id.rb_provider_anthropic -> AiProviderType.ANTHROPIC
            R.id.rb_provider_compatible -> AiProviderType.COMPATIBLE
            else -> AiProviderType.BUILTIN
        }
    }

    private fun personaFromCheckedId(checkedId: Int): CompanionPersonaPreset {
        return when (checkedId) {
            R.id.rb_persona_girlfriend -> CompanionPersonaPreset.LIVELY_GIRLFRIEND
            R.id.rb_persona_butler -> CompanionPersonaPreset.COOL_BUTLER
            R.id.rb_persona_friend -> CompanionPersonaPreset.SAVAGE_FRIEND
            else -> CompanionPersonaPreset.GENTLE_BOYFRIEND
        }
    }
}
