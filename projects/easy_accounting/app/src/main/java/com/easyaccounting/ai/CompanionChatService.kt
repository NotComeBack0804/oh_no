package com.easyaccounting.ai

import android.content.Context
import com.easyaccounting.data.entity.AiChatMessage
import com.easyaccounting.data.entity.AiIntentType
import com.easyaccounting.data.entity.Bill
import com.easyaccounting.data.entity.Category
import com.easyaccounting.data.entity.CategoryType
import com.easyaccounting.data.entity.ChatMessageRole
import com.easyaccounting.data.entity.Income
import com.easyaccounting.data.repository.AiChatRepository
import com.easyaccounting.data.repository.BillRepository
import com.easyaccounting.data.repository.CategoryRepository
import com.easyaccounting.data.repository.IncomeRepository
import com.easyaccounting.util.FormatUtils

class CompanionChatService(
    context: Context,
    private val aiChatRepository: AiChatRepository,
    private val billRepository: BillRepository,
    private val incomeRepository: IncomeRepository,
    private val categoryRepository: CategoryRepository
) {
    private val settingsStore = AiSettingsStore(context)
    private val builtinEngine = BuiltinCompanionEngine()
    private val llmGateway = LlmGateway()

    suspend fun sendMessage(
        conversationId: String,
        userInput: String
    ) {
        val normalizedInput = userInput.trim()
        if (normalizedInput.isBlank()) return

        val recentHistory = aiChatRepository.getRecentMessages(conversationId, 8).reversed()
        val effectiveInput = enrichClarificationInput(normalizedInput, recentHistory)
        val config = settingsStore.getConfig()

        aiChatRepository.insertMessage(
            AiChatMessage(
                conversationId = conversationId,
                role = ChatMessageRole.USER,
                content = normalizedInput
            )
        )

        val assistantSeed = AiChatMessage(
            conversationId = conversationId,
            role = ChatMessageRole.ASSISTANT,
            content = "...",
            sourceLabel = config.displayModelName()
        )
        val assistantId = aiChatRepository.insertMessage(assistantSeed)
        var assistantMessage = assistantSeed.copy(id = assistantId)
        var lastPreview = assistantMessage.content

        val expenseCategories = categoryRepository.getCategoriesByTypeSync(CategoryType.EXPENSE)
        val incomeCategories = categoryRepository.getCategoriesByTypeSync(CategoryType.INCOME)

        val structuredResponse = resolveResponse(
            config = config,
            history = recentHistory,
            effectiveInput = effectiveInput,
            expenseCategories = expenseCategories,
            incomeCategories = incomeCategories,
            onPartialReplyText = { preview ->
                val normalizedPreview = preview.ifBlank { "..." }
                if (normalizedPreview != lastPreview) {
                    lastPreview = normalizedPreview
                    assistantMessage = assistantMessage.copy(content = normalizedPreview)
                    aiChatRepository.updateMessage(assistantMessage)
                }
            }
        )

        persistAssistantResult(
            assistantMessage = assistantMessage,
            userInput = normalizedInput,
            structuredResponse = structuredResponse,
            expenseCategories = expenseCategories,
            incomeCategories = incomeCategories
        )
    }

    private suspend fun resolveResponse(
        config: AiProviderConfig,
        history: List<AiChatMessage>,
        effectiveInput: String,
        expenseCategories: List<Category>,
        incomeCategories: List<Category>,
        onPartialReplyText: suspend (String) -> Unit
    ): CompanionStructuredResponse {
        val expenseNames = expenseCategories.map { it.name }
        val incomeNames = incomeCategories.map { it.name }

        if (config.providerType == AiProviderType.BUILTIN) {
            return builtinEngine.reply(effectiveInput, config, expenseNames, incomeNames)
        }

        if (config.resolvedEndpoint().isBlank() || config.resolvedModel().isBlank() || config.resolvedApiKey().isBlank()) {
            return unavailableResponse(
                config = config,
                message = "当前选中的模型尚未配置完成。先去 AI 设置里测试连接，再继续聊天。"
            )
        }

        val prompt = CompanionPromptFactory.buildSystemPrompt(config, expenseNames, incomeNames)

        return runCatching {
            llmGateway.requestStructuredReply(
                config = config,
                systemPrompt = prompt,
                history = history,
                latestUserInput = effectiveInput,
                onPartialReplyText = onPartialReplyText
            )
        }.getOrElse { error ->
            unavailableResponse(
                config = config,
                message = "当前模型暂时连不上，我没有自动切到内置智脑。你可以去设置页点“测试连接”看具体报错。${error.message?.let { "（$it）" } ?: ""}"
            )
        }
    }

    private fun unavailableResponse(
        config: AiProviderConfig,
        message: String
    ): CompanionStructuredResponse {
        return CompanionStructuredResponse(
            replyText = message,
            intentType = AiIntentType.CHAT,
            sourceLabel = config.displayModelName()
        )
    }

    private suspend fun persistAssistantResult(
        assistantMessage: AiChatMessage,
        userInput: String,
        structuredResponse: CompanionStructuredResponse,
        expenseCategories: List<Category>,
        incomeCategories: List<Category>
    ) {
        var linkedBillId: Long? = null
        var linkedIncomeId: Long? = null
        var summary: String? = null
        var resolvedCategoryLabel = structuredResponse.category

        if (!structuredResponse.needsClarification && structuredResponse.amount != null) {
            when (structuredResponse.intentType) {
                AiIntentType.EXPENSE -> {
                    val category = resolveCategory(
                        source = structuredResponse.category,
                        fallbackText = userInput,
                        categories = expenseCategories,
                        defaultName = "其他"
                    )
                    resolvedCategoryLabel = category?.name ?: "其他"
                    linkedBillId = billRepository.insertBill(
                        Bill(
                            amount = structuredResponse.amount,
                            categoryId = category?.id,
                            date = System.currentTimeMillis(),
                            remark = structuredResponse.remark?.takeIf { it.isNotBlank() } ?: userInput.take(32),
                            accountId = null
                        )
                    )
                    summary = "已记入${resolvedCategoryLabel} -${FormatUtils.formatAmount(structuredResponse.amount)} 元"
                }

                AiIntentType.INCOME -> {
                    val category = resolveCategory(
                        source = structuredResponse.category,
                        fallbackText = userInput,
                        categories = incomeCategories,
                        defaultName = "其他"
                    )
                    resolvedCategoryLabel = category?.name ?: "其他"
                    linkedIncomeId = incomeRepository.insertIncome(
                        Income(
                            amount = structuredResponse.amount,
                            source = resolvedCategoryLabel,
                            date = System.currentTimeMillis(),
                            remark = structuredResponse.remark?.takeIf { it.isNotBlank() } ?: userInput.take(32),
                            accountId = null
                        )
                    )
                    summary = "已记入${resolvedCategoryLabel} +${FormatUtils.formatAmount(structuredResponse.amount)} 元"
                }

                else -> Unit
            }
        }

        val fallbackPreview = assistantMessage.content.takeIf { it.isNotBlank() && it != "..." }
        val assistantText = when {
            structuredResponse.needsClarification -> structuredResponse.clarificationQuestion ?: structuredResponse.replyText
            structuredResponse.replyText.isNotBlank() -> structuredResponse.replyText
            !fallbackPreview.isNullOrBlank() -> fallbackPreview
            else -> "我先记住你这句话，等你想说的时候我们再继续。"
        }

        aiChatRepository.updateMessage(
            assistantMessage.copy(
                content = assistantText,
                intentType = structuredResponse.intentType,
                ledgerAmount = structuredResponse.amount,
                ledgerCategory = resolvedCategoryLabel,
                ledgerSummary = summary,
                linkedBillId = linkedBillId,
                linkedIncomeId = linkedIncomeId,
                needsClarification = structuredResponse.needsClarification,
                sourceLabel = structuredResponse.sourceLabel
            )
        )
    }

    private fun enrichClarificationInput(
        userInput: String,
        history: List<AiChatMessage>
    ): String {
        val amountOnlyRegex = Regex("""^\s*\d+(?:\.\d{1,2})?\s*(万|w|W|元|块|块钱|人民币|rmb|RMB)?\s*$""")
        if (!amountOnlyRegex.matches(userInput)) return userInput

        val lastAssistant = history.lastOrNull { it.role == ChatMessageRole.ASSISTANT } ?: return userInput
        if (!lastAssistant.needsClarification) return userInput

        val lastUser = history.lastOrNull { it.role == ChatMessageRole.USER }?.content.orEmpty()
        val incomeHints = listOf("工资", "收入", "到账", "收到", "奖金", "报销", "赚了", "进账", "收款", "红包", "提成", "分红", "退款", "返现", "捡到")
        val incomeCategories = setOf("工资", "奖金", "投资收益")
        val intentLabel = if (
            incomeHints.any { lastUser.contains(it) } ||
            incomeCategories.contains(lastAssistant.ledgerCategory)
        ) {
            "收入"
        } else {
            "支出"
        }

        return listOf(
            intentLabel,
            lastAssistant.ledgerCategory.orEmpty(),
            lastUser,
            userInput
        ).filter { it.isNotBlank() }.joinToString(" ")
    }

    private fun resolveCategory(
        source: String?,
        fallbackText: String,
        categories: List<Category>,
        defaultName: String
    ): Category? {
        if (categories.isEmpty()) return null

        val trimmedSource = source?.trim().orEmpty()
        val direct = categories.firstOrNull { category ->
            category.name.equals(trimmedSource, ignoreCase = true)
        }
        if (direct != null) return direct

        val contains = categories.firstOrNull { category ->
            trimmedSource.contains(category.name, ignoreCase = true) ||
                fallbackText.contains(category.name, ignoreCase = true)
        }
        if (contains != null) return contains

        return categories.firstOrNull { it.name == defaultName } ?: categories.firstOrNull()
    }
}
