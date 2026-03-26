package com.easyaccounting.ai

import com.easyaccounting.data.entity.AiIntentType

enum class AiProviderType(
    val displayName: String,
    val defaultEndpoint: String,
    val requiresApiKey: Boolean
) {
    BUILTIN("内置智脑", "", false),
    PRESET_GPT54("GPT-5.4", "", false),
    OPENAI("自定义 OpenAI", "https://api.openai.com/v1/chat/completions", true),
    ANTHROPIC("自定义 Claude", "https://api.anthropic.com/v1/messages", true),
    COMPATIBLE("自定义兼容协议", "", true)
}

enum class CompanionPersonaPreset(
    val displayName: String,
    val defaultName: String,
    val stylePrompt: String
) {
    GENTLE_BOYFRIEND(
        displayName = "温柔男友",
        defaultName = "阿序",
        stylePrompt = "像真实男友一样聊天。先理解情绪、顺着话题自然回应，讲话温柔、轻松、会主动关心，不油腻，不像客服。"
    ),
    LIVELY_GIRLFRIEND(
        displayName = "元气女友",
        defaultName = "小满",
        stylePrompt = "像真实女友一样聊天。语气有活力、会接梗、会安慰，也会鼓励人，聊天自然有陪伴感，不要像机器人。"
    ),
    COOL_BUTLER(
        displayName = "高冷管家",
        defaultName = "K 管家",
        stylePrompt = "像一个克制但可靠的私人管家。表达简洁、成熟、有分寸，先陪聊再顺手记账，不要生硬播报。"
    ),
    SAVAGE_FRIEND(
        displayName = "毒舌损友",
        defaultName = "阿损",
        stylePrompt = "像一个会吐槽但很护短的多年好友。可以轻微打趣，但要让用户感到被理解、被接住，绝不能刻薄。"
    );

    companion object {
        fun fromName(name: String?): CompanionPersonaPreset {
            return entries.firstOrNull { it.name == name } ?: GENTLE_BOYFRIEND
        }
    }
}

object AiPresetChannels {
    private const val GPT54_BASE_URL = "https://ai.td.ee/v1"
    private const val GPT54_MODEL = "gpt-5.4"
    private const val GPT54_API_KEY = "sk-b6670a1a689ab4aa41cb9300c6b295694460b3de8426e3da880c636ecd6184cb"

    fun resolvedEndpoint(providerType: AiProviderType, rawEndpoint: String): String {
        return when (providerType) {
            AiProviderType.PRESET_GPT54 -> GPT54_BASE_URL
            else -> rawEndpoint.trim().ifBlank { providerType.defaultEndpoint }
        }
    }

    fun resolvedModel(providerType: AiProviderType, rawModel: String): String {
        return when (providerType) {
            AiProviderType.PRESET_GPT54 -> GPT54_MODEL
            else -> rawModel.trim()
        }
    }

    fun resolvedApiKey(providerType: AiProviderType, rawApiKey: String): String {
        return when (providerType) {
            AiProviderType.PRESET_GPT54 -> GPT54_API_KEY
            else -> rawApiKey.trim()
        }
    }

    fun displayModelName(providerType: AiProviderType, rawModel: String): String {
        return when (providerType) {
            AiProviderType.PRESET_GPT54 -> GPT54_MODEL
            AiProviderType.BUILTIN -> "内置智脑"
            else -> rawModel.trim().ifBlank { providerType.displayName }
        }
    }
}

data class AiProviderConfig(
    val providerType: AiProviderType = AiProviderType.PRESET_GPT54,
    val endpoint: String = "",
    val model: String = "",
    val apiKey: String = "",
    val personaPreset: CompanionPersonaPreset = CompanionPersonaPreset.GENTLE_BOYFRIEND,
    val companionName: String = CompanionPersonaPreset.GENTLE_BOYFRIEND.defaultName,
    val customPrompt: String = ""
) {
    fun resolvedEndpoint(): String {
        return AiPresetChannels.resolvedEndpoint(providerType, endpoint)
    }

    fun resolvedRequestUrl(): String {
        val raw = resolvedEndpoint().removeSuffix("/")
        if (raw.isBlank()) return raw

        return when (providerType) {
            AiProviderType.BUILTIN -> raw
            AiProviderType.PRESET_GPT54,
            AiProviderType.OPENAI,
            AiProviderType.COMPATIBLE -> when {
                raw.lowercase().endsWith("/chat/completions") -> raw
                raw.lowercase().matches(Regex(""".*/v\d+""")) -> "$raw/chat/completions"
                else -> raw
            }

            AiProviderType.ANTHROPIC -> when {
                raw.lowercase().endsWith("/messages") -> raw
                raw.lowercase().matches(Regex(""".*/v\d+""")) -> "$raw/messages"
                else -> raw
            }
        }
    }

    fun resolvedModel(): String {
        return AiPresetChannels.resolvedModel(providerType, model)
    }

    fun resolvedApiKey(): String {
        return AiPresetChannels.resolvedApiKey(providerType, apiKey)
    }

    fun resolvedCompanionName(): String {
        return companionName.trim().ifBlank { personaPreset.defaultName }
    }

    fun providerLabel(): String = displayModelName()

    fun personaLabel(): String = personaPreset.displayName

    fun displayModelName(): String = AiPresetChannels.displayModelName(providerType, model)
}

data class CompanionStructuredResponse(
    val replyText: String,
    val intentType: AiIntentType,
    val amount: Double? = null,
    val category: String? = null,
    val remark: String? = null,
    val needsClarification: Boolean = false,
    val clarificationQuestion: String? = null,
    val sourceLabel: String = "AI"
)
