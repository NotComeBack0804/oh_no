package com.easyaccounting.ai

object SensitiveDataMasker {
    private val bankCardRegex = Regex("(?<!\\d)\\d{12,19}(?!\\d)")
    private val passwordRegex = Regex(
        "(密码|支付密码|口令|passcode|password)\\s*[:：]?\\s*\\S+",
        RegexOption.IGNORE_CASE
    )

    fun mask(input: String): String {
        if (input.isBlank()) return input

        return input
            .replace(bankCardRegex, "[BANK_CARD]")
            .replace(passwordRegex) { match ->
                val key = match.value.substringBefore(':').substringBefore('：').trim()
                "$key [REDACTED]"
            }
    }
}
