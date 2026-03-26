package com.easyaccounting.ai

import android.content.Context
import com.easyaccounting.util.SecretStoreUtils

class AiSettingsStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getConfig(): AiProviderConfig {
        val rawProvider = prefs.getString(KEY_PROVIDER, null)
        val providerType = runCatching {
            AiProviderType.valueOf(rawProvider ?: AiProviderType.PRESET_GPT54.name)
        }.getOrDefault(AiProviderType.PRESET_GPT54)

        val personaPreset = runCatching {
            CompanionPersonaPreset.valueOf(
                prefs.getString(
                    KEY_PERSONA,
                    CompanionPersonaPreset.GENTLE_BOYFRIEND.name
                ).orEmpty()
            )
        }.getOrDefault(CompanionPersonaPreset.GENTLE_BOYFRIEND)

        val apiKey = prefs.getString(KEY_API_KEY, null)
            ?.takeIf { it.isNotBlank() }
            ?.let { encrypted -> runCatching { SecretStoreUtils.decrypt(encrypted) }.getOrDefault("") }
            .orEmpty()

        val endpoint = prefs.getString(KEY_ENDPOINT, "").orEmpty()
        val model = prefs.getString(KEY_MODEL, "").orEmpty()
        val migratedProvider = if (
            providerType == AiProviderType.COMPATIBLE &&
            endpoint.trim() == "https://ai.td.ee/v1" &&
            model.trim() == "gpt-5.4"
        ) {
            AiProviderType.PRESET_GPT54
        } else {
            providerType
        }

        return AiProviderConfig(
            providerType = migratedProvider,
            endpoint = endpoint,
            model = model,
            apiKey = apiKey,
            personaPreset = personaPreset,
            companionName = prefs.getString(KEY_NAME, personaPreset.defaultName).orEmpty(),
            customPrompt = prefs.getString(KEY_CUSTOM_PROMPT, "").orEmpty()
        )
    }

    fun saveConfig(config: AiProviderConfig) {
        val storedConfig = when (config.providerType) {
            AiProviderType.PRESET_GPT54 -> config.copy(endpoint = "", model = "", apiKey = "")
            else -> config
        }

        prefs.edit()
            .putString(KEY_PROVIDER, storedConfig.providerType.name)
            .putString(KEY_ENDPOINT, storedConfig.endpoint.trim())
            .putString(KEY_MODEL, storedConfig.model.trim())
            .putString(
                KEY_API_KEY,
                storedConfig.apiKey.trim().takeIf { it.isNotBlank() }?.let(SecretStoreUtils::encrypt)
            )
            .putString(KEY_PERSONA, storedConfig.personaPreset.name)
            .putString(KEY_NAME, storedConfig.resolvedCompanionName())
            .putString(KEY_CUSTOM_PROMPT, storedConfig.customPrompt.trim())
            .apply()
    }

    fun buildModeHint(): String {
        val config = getConfig()
        return "当前模型：${config.displayModelName()} · ${config.personaLabel()} ${config.resolvedCompanionName()}"
    }

    companion object {
        private const val PREFS_NAME = "ai_companion_prefs"
        private const val KEY_PROVIDER = "provider_type"
        private const val KEY_ENDPOINT = "provider_endpoint"
        private const val KEY_MODEL = "provider_model"
        private const val KEY_API_KEY = "provider_api_key"
        private const val KEY_PERSONA = "persona_preset"
        private const val KEY_NAME = "companion_name"
        private const val KEY_CUSTOM_PROMPT = "custom_prompt"
    }
}
