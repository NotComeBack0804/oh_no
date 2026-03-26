package com.easyaccounting.ai

import com.easyaccounting.data.entity.AiIntentType
import java.util.Locale
import kotlin.math.roundToLong

class BuiltinCompanionEngine {
    private val amountRegex = Regex("""(\d+(?:\.\d{1,2})?)\s*(万|w|W|元|块|块钱|人民币|rmb|RMB)?""")
    private val moneyOnlyRegex = Regex("""^\s*\d+(?:\.\d{1,2})?\s*(万|w|W|元|块|块钱|人民币|rmb|RMB)?\s*$""")

    private val planningKeywords = listOf("想买", "准备买", "打算买", "如果买", "想入手", "考虑买")
    private val jokeKeywords = listOf("空气", "做梦", "幻想", "白日梦", "如果我有钱", "天上掉钱")

    private val expenseHints = listOf(
        "花了", "买了", "吃了", "付款", "支付", "消费", "花费", "打车",
        "交了", "点了", "充了", "转账给", "付了", "刷了", "报了名", "订了"
    )
    private val incomeHints = listOf(
        "工资", "收入", "到账", "收到了", "奖金", "报销", "赚了", "进账", "收款",
        "红包", "发了工资", "提成", "分红", "捡到", "捡了", "中奖了", "中奖",
        "退款", "退回", "返现", "返利", "赔偿", "退税", "卖了", "卖掉", "回款"
    )

    private val expenseCategoryKeywords = linkedMapOf(
        "餐饮" to listOf("吃", "饭", "早餐", "午餐", "晚餐", "夜宵", "外卖", "奶茶", "咖啡", "黄焖鸡", "火锅"),
        "交通" to listOf("打车", "地铁", "公交", "高铁", "机票", "油费", "停车", "过路费"),
        "购物" to listOf("淘宝", "京东", "拼多多", "衣服", "鞋", "包", "耳机", "手机", "买"),
        "娱乐" to listOf("电影", "游戏", "唱歌", "演唱会", "酒吧", "旅游", "桌游"),
        "医疗" to listOf("医院", "挂号", "药", "看病", "体检", "门诊"),
        "教育" to listOf("课程", "学费", "报名", "考试", "培训", "教材")
    )

    private val incomeCategoryKeywords = linkedMapOf(
        "工资" to listOf("工资", "薪资", "发薪", "发了工资"),
        "奖金" to listOf("奖金", "年终奖", "提成", "绩效", "中奖", "中奖了"),
        "投资收益" to listOf("理财", "基金", "股票", "投资", "分红"),
        "其他" to listOf(
            "报销", "红包", "收款", "到账", "捡到", "捡了", "退款", "退回",
            "返现", "返利", "赔偿", "退税", "卖了", "卖掉", "回款"
        )
    )

    fun reply(
        input: String,
        config: AiProviderConfig,
        expenseCategories: List<String>,
        incomeCategories: List<String>
    ): CompanionStructuredResponse {
        val cleanInput = input.trim()
        if (cleanInput.isBlank()) {
            return buildChatResponse(
                config = config,
                text = "想和我聊点什么？也可以直接告诉我一笔消费或收入。"
            )
        }

        val amount = extractAmount(cleanInput)
        if (looksLikeChat(cleanInput, amount)) {
            return buildChatResponse(
                config = config,
                text = "这句更像是在和我聊天，我先不替你落账。想记账的话，直接告诉我金额就行。"
            )
        }

        val intent = detectIntent(cleanInput, amount)
        if ((intent == AiIntentType.EXPENSE || intent == AiIntentType.INCOME) && amount == null) {
            val category = if (intent == AiIntentType.EXPENSE) {
                resolveCategory(cleanInput, expenseCategories, expenseCategoryKeywords)
            } else {
                resolveCategory(cleanInput, incomeCategories, incomeCategoryKeywords)
            }
            val question = buildClarifyReply(config, intent, category)
            return CompanionStructuredResponse(
                replyText = question,
                intentType = AiIntentType.CLARIFY,
                category = category,
                remark = extractRemark(cleanInput),
                needsClarification = true,
                clarificationQuestion = question,
                sourceLabel = AiProviderType.BUILTIN.displayName
            )
        }

        return when (intent) {
            AiIntentType.EXPENSE -> {
                val category = resolveCategory(cleanInput, expenseCategories, expenseCategoryKeywords)
                    ?: expenseCategories.firstOrNull()
                    ?: "其他"
                CompanionStructuredResponse(
                    replyText = buildExpenseReply(config, category, amount ?: 0.0),
                    intentType = AiIntentType.EXPENSE,
                    amount = amount,
                    category = category,
                    remark = extractRemark(cleanInput),
                    sourceLabel = AiProviderType.BUILTIN.displayName
                )
            }

            AiIntentType.INCOME -> {
                val category = resolveCategory(cleanInput, incomeCategories, incomeCategoryKeywords)
                    ?: incomeCategories.firstOrNull()
                    ?: "其他"
                CompanionStructuredResponse(
                    replyText = buildIncomeReply(config, category, amount ?: 0.0),
                    intentType = AiIntentType.INCOME,
                    amount = amount,
                    category = category,
                    remark = extractRemark(cleanInput),
                    sourceLabel = AiProviderType.BUILTIN.displayName
                )
            }

            else -> buildChatResponse(
                config = config,
                text = "这句更像是在和我聊天，我先不替你落账。想记账的话，直接告诉我金额就行。"
            )
        }
    }

    private fun buildChatResponse(config: AiProviderConfig, text: String): CompanionStructuredResponse {
        return CompanionStructuredResponse(
            replyText = buildChatReply(config, text),
            intentType = AiIntentType.CHAT,
            sourceLabel = AiProviderType.BUILTIN.displayName
        )
    }

    private fun looksLikeChat(input: String, amount: Double?): Boolean {
        if (planningKeywords.any { input.contains(it) } && amount == null) return true
        if (jokeKeywords.any { input.contains(it) }) return true
        return moneyOnlyRegex.matches(input).not() &&
            expenseHints.none { input.contains(it) } &&
            incomeHints.none { input.contains(it) } &&
            amount == null
    }

    private fun extractAmount(input: String): Double? {
        val matches = amountRegex.findAll(input)
            .mapNotNull { result ->
                val rawAmount = result.groupValues[1].toDoubleOrNull() ?: return@mapNotNull null
                val unit = result.groupValues.getOrNull(2).orEmpty()
                val resolved = if (unit == "万" || unit.equals("w", ignoreCase = true)) {
                    rawAmount * 10_000
                } else {
                    rawAmount
                }
                if (resolved <= 0) return@mapNotNull null
                (resolved * 100).roundToLong() / 100.0
            }
            .toList()

        return matches.maxOrNull()
    }

    private fun detectIntent(input: String, amount: Double?): AiIntentType {
        val hasExpense = expenseHints.any { input.contains(it) }
        val hasIncome = incomeHints.any { input.contains(it) }

        return when {
            hasIncome && !hasExpense -> AiIntentType.INCOME
            hasExpense && !hasIncome -> AiIntentType.EXPENSE
            amount != null && input.contains("工资") -> AiIntentType.INCOME
            amount != null && incomeCategoryKeywords.values.flatten().any { input.contains(it) } -> AiIntentType.INCOME
            amount != null && expenseCategoryKeywords.values.flatten().any { input.contains(it) } -> AiIntentType.EXPENSE
            amount != null && input.contains("捡") -> AiIntentType.INCOME
            amount != null && input.contains("退") && input.contains("款") -> AiIntentType.INCOME
            amount != null && input.contains("返现") -> AiIntentType.INCOME
            else -> AiIntentType.CHAT
        }
    }

    private fun resolveCategory(
        input: String,
        allowedCategories: List<String>,
        keywordMap: Map<String, List<String>>
    ): String? {
        val direct = allowedCategories.firstOrNull { category ->
            input.contains(category, ignoreCase = true)
        }
        if (direct != null) return direct

        val matchedCategory = keywordMap.entries.firstOrNull { (_, keywords) ->
            keywords.any { keyword -> input.contains(keyword, ignoreCase = true) }
        }?.key

        return when {
            matchedCategory == null -> allowedCategories.firstOrNull()
            allowedCategories.contains(matchedCategory) -> matchedCategory
            else -> allowedCategories.firstOrNull { it.contains(matchedCategory) || matchedCategory.contains(it) }
                ?: matchedCategory
        }
    }

    private fun extractRemark(input: String): String {
        return input
            .replace(amountRegex, "")
            .replace("今天", "")
            .replace("刚刚", "")
            .replace("刚", "")
            .replace("花了", "")
            .replace("买了", "")
            .replace("吃了", "")
            .replace("发了", "")
            .replace("收到了", "")
            .replace("到账", "")
            .replace("捡到了", "")
            .replace("捡到", "")
            .replace("捡了", "")
            .replace("退款", "")
            .replace("退回", "")
            .replace("返现", "")
            .replace("返利", "")
            .trim()
            .ifBlank { input.take(24) }
            .take(32)
    }

    private fun buildClarifyReply(
        config: AiProviderConfig,
        intentType: AiIntentType,
        category: String?
    ): String {
        val label = category ?: if (intentType == AiIntentType.INCOME) "收入" else "这笔消费"
        return when (config.personaPreset) {
            CompanionPersonaPreset.GENTLE_BOYFRIEND ->
                "这笔${label}我先替你记在心里，不过金额还差一点。你告诉我是多少钱，我马上补上。"

            CompanionPersonaPreset.LIVELY_GIRLFRIEND ->
                "我就差最后一步啦，${label}到底是多少钱呀？告诉我我就立刻帮你记好。"

            CompanionPersonaPreset.COOL_BUTLER ->
                "信息尚不完整，请补充${label}金额。"

            CompanionPersonaPreset.SAVAGE_FRIEND ->
                "别只说一半呀，${label}到底多少钱？你不报数我怎么替你记。"
        }
    }

    private fun buildExpenseReply(config: AiProviderConfig, category: String, amount: Double): String {
        return when (config.personaPreset) {
            CompanionPersonaPreset.GENTLE_BOYFRIEND ->
                "辛苦啦，这笔 ${category} ${formatAmount(amount)} 元我已经替你记好了。"

            CompanionPersonaPreset.LIVELY_GIRLFRIEND ->
                "收到收到，这笔${category}支出 ${formatAmount(amount)} 元已经进账本啦。"

            CompanionPersonaPreset.COOL_BUTLER ->
                "已记录。${category}支出 ${formatAmount(amount)} 元。"

            CompanionPersonaPreset.SAVAGE_FRIEND ->
                "又花出去 ${formatAmount(amount)} 元，不过别慌，这笔${category}我已经给你记上了。"
        }
    }

    private fun buildIncomeReply(config: AiProviderConfig, category: String, amount: Double): String {
        return when (config.personaPreset) {
            CompanionPersonaPreset.GENTLE_BOYFRIEND ->
                "太好了，这笔 ${category} 收入 ${formatAmount(amount)} 元已经替你收进账本。"

            CompanionPersonaPreset.LIVELY_GIRLFRIEND ->
                "哇，有进账耶，${category} ${formatAmount(amount)} 元我已经帮你记下来啦。"

            CompanionPersonaPreset.COOL_BUTLER ->
                "已记录。${category}收入 ${formatAmount(amount)} 元。"

            CompanionPersonaPreset.SAVAGE_FRIEND ->
                "行，这次终于不是只会花钱了。${category} ${formatAmount(amount)} 元我已经替你记上。"
        }
    }

    private fun buildChatReply(config: AiProviderConfig, fallback: String): String {
        return when (config.personaPreset) {
            CompanionPersonaPreset.GENTLE_BOYFRIEND -> fallback
            CompanionPersonaPreset.LIVELY_GIRLFRIEND -> "$fallback 我继续陪你。"
            CompanionPersonaPreset.COOL_BUTLER -> fallback
            CompanionPersonaPreset.SAVAGE_FRIEND -> "$fallback 别慌，我还在。"
        }
    }

    private fun formatAmount(amount: Double): String {
        return String.format(Locale.US, "%.2f", amount)
    }
}
