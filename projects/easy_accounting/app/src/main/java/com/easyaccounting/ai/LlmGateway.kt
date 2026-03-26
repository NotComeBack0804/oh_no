package com.easyaccounting.ai

import com.easyaccounting.data.entity.AiChatMessage
import com.easyaccounting.data.entity.AiIntentType
import com.easyaccounting.data.entity.ChatMessageRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class LlmGateway {
    suspend fun requestStructuredReply(
        config: AiProviderConfig,
        systemPrompt: String,
        history: List<AiChatMessage>,
        latestUserInput: String,
        onPartialReplyText: suspend (String) -> Unit = {}
    ): CompanionStructuredResponse = withContext(Dispatchers.IO) {
        when (config.providerType) {
            AiProviderType.OPENAI,
            AiProviderType.COMPATIBLE,
            AiProviderType.PRESET_GPT54 -> requestOpenAiCompatible(
                config = config,
                systemPrompt = systemPrompt,
                history = history,
                latestUserInput = latestUserInput,
                onPartialReplyText = onPartialReplyText
            )

            AiProviderType.ANTHROPIC -> requestAnthropic(
                config = config,
                systemPrompt = systemPrompt,
                history = history,
                latestUserInput = latestUserInput,
                onPartialReplyText = onPartialReplyText
            )

            AiProviderType.BUILTIN -> error("Builtin provider should not call remote gateway")
        }
    }

    suspend fun testConnection(config: AiProviderConfig): String = withContext(Dispatchers.IO) {
        when (config.providerType) {
            AiProviderType.BUILTIN -> "内置智脑可用，无需联网。你可以直接开始聊天记账。"
            AiProviderType.OPENAI,
            AiProviderType.COMPATIBLE,
            AiProviderType.PRESET_GPT54 -> testOpenAiCompatible(config)

            AiProviderType.ANTHROPIC -> testAnthropic(config)
        }
    }

    private suspend fun requestOpenAiCompatible(
        config: AiProviderConfig,
        systemPrompt: String,
        history: List<AiChatMessage>,
        latestUserInput: String,
        onPartialReplyText: suspend (String) -> Unit
    ): CompanionStructuredResponse {
        validateRemoteConfig(config)

        val messages = buildOpenAiMessages(
            systemPrompt = systemPrompt,
            history = history,
            latestUserInput = latestUserInput
        )

        return try {
            val streamedPayload = postOpenAiCompatibleStream(
                url = config.resolvedRequestUrl(),
                headers = openAiHeaders(config),
                body = buildOpenAiRequestBody(
                    model = config.resolvedModel(),
                    messages = messages,
                    stream = true
                ),
                onPartialReplyText = onPartialReplyText
            )
            parseStructuredPayload(streamedPayload, config.displayModelName())
        } catch (error: Exception) {
            if (!shouldFallbackToNonStreaming(error)) throw error

            val response = postJson(
                url = config.resolvedRequestUrl(),
                headers = openAiHeaders(config),
                body = buildOpenAiRequestBody(
                    model = config.resolvedModel(),
                    messages = messages,
                    stream = false
                ),
                readTimeoutMillis = 90_000
            )

            val content = JSONObject(response)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

            val structured = parseStructuredPayload(content, config.displayModelName())
            if (structured.replyText.isNotBlank()) {
                onPartialReplyText(structured.replyText)
            }
            structured
        }
    }

    private suspend fun requestAnthropic(
        config: AiProviderConfig,
        systemPrompt: String,
        history: List<AiChatMessage>,
        latestUserInput: String,
        onPartialReplyText: suspend (String) -> Unit
    ): CompanionStructuredResponse {
        validateRemoteConfig(config)

        val messages = JSONArray()
        history.forEach { message ->
            messages.put(
                JSONObject()
                    .put("role", if (message.role == ChatMessageRole.USER) "user" else "assistant")
                    .put("content", compactMessageContent(message.content))
            )
        }
        messages.put(
            JSONObject()
                .put("role", "user")
                .put("content", SensitiveDataMasker.mask(latestUserInput))
        )

        val body = JSONObject()
            .put("model", config.resolvedModel())
            .put("max_tokens", 280)
            .put("temperature", 0.45)
            .put("system", systemPrompt)
            .put("messages", messages)

        val response = postJson(
            url = config.resolvedRequestUrl(),
            headers = mapOf(
                "x-api-key" to config.resolvedApiKey(),
                "anthropic-version" to "2023-06-01",
                "Content-Type" to "application/json"
            ),
            body = body,
            readTimeoutMillis = 90_000
        )

        val responseObject = JSONObject(response)
        val contentArray = responseObject.getJSONArray("content")
        val text = buildString {
            for (index in 0 until contentArray.length()) {
                val item = contentArray.getJSONObject(index)
                if (item.optString("type") == "text") {
                    append(item.optString("text"))
                }
            }
        }

        val structured = parseStructuredPayload(text, config.displayModelName())
        if (structured.replyText.isNotBlank()) {
            onPartialReplyText(structured.replyText)
        }
        return structured
    }

    private fun testOpenAiCompatible(config: AiProviderConfig): String {
        validateRemoteConfig(config)

        val body = JSONObject()
            .put("model", config.resolvedModel())
            .put("temperature", 0)
            .put("max_tokens", 12)
            .put(
                "messages",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put("content", "Reply with OK only.")
                )
            )

        val response = postJson(
            url = config.resolvedRequestUrl(),
            headers = openAiHeaders(config),
            body = body
        )

        val reply = JSONObject(response)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .optString("content")
            .trim()
            .ifBlank { "已收到模型响应" }

        return "连接成功：$reply"
    }

    private fun testAnthropic(config: AiProviderConfig): String {
        validateRemoteConfig(config)

        val body = JSONObject()
            .put("model", config.resolvedModel())
            .put("max_tokens", 12)
            .put("temperature", 0)
            .put(
                "messages",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put("content", "Reply with OK only.")
                )
            )

        val response = postJson(
            url = config.resolvedRequestUrl(),
            headers = mapOf(
                "x-api-key" to config.resolvedApiKey(),
                "anthropic-version" to "2023-06-01",
                "Content-Type" to "application/json"
            ),
            body = body
        )

        val contentArray = JSONObject(response).getJSONArray("content")
        val reply = buildString {
            for (index in 0 until contentArray.length()) {
                val item = contentArray.getJSONObject(index)
                if (item.optString("type") == "text") {
                    append(item.optString("text"))
                }
            }
        }.trim().ifBlank { "已收到模型响应" }

        return "连接成功：$reply"
    }

    private fun buildOpenAiMessages(
        systemPrompt: String,
        history: List<AiChatMessage>,
        latestUserInput: String
    ): JSONArray {
        val messages = JSONArray()
            .put(
                JSONObject()
                    .put("role", "system")
                    .put("content", systemPrompt)
            )

        history.forEach { message ->
            messages.put(
                JSONObject()
                    .put("role", if (message.role == ChatMessageRole.USER) "user" else "assistant")
                    .put("content", compactMessageContent(message.content))
            )
        }

        messages.put(
            JSONObject()
                .put("role", "user")
                .put("content", SensitiveDataMasker.mask(latestUserInput))
        )
        return messages
    }

    private fun buildOpenAiRequestBody(
        model: String,
        messages: JSONArray,
        stream: Boolean
    ): JSONObject {
        return JSONObject()
            .put("model", model)
            .put("temperature", 0.45)
            .put("max_tokens", 280)
            .put("messages", messages)
            .put("response_format", JSONObject().put("type", "json_object"))
            .put("stream", stream)
    }

    private fun openAiHeaders(config: AiProviderConfig): Map<String, String> {
        return mapOf(
            "Authorization" to "Bearer ${config.resolvedApiKey()}",
            "Content-Type" to "application/json"
        )
    }

    private fun compactMessageContent(raw: String): String {
        return SensitiveDataMasker.mask(raw).take(280)
    }

    private fun validateRemoteConfig(config: AiProviderConfig) {
        when {
            config.resolvedEndpoint().isBlank() -> {
                throw IllegalArgumentException("请先填写 Endpoint。")
            }

            config.resolvedModel().isBlank() -> {
                throw IllegalArgumentException("请先填写模型名称。")
            }

            config.resolvedApiKey().isBlank() -> {
                throw IllegalArgumentException("请先填写 API Key。")
            }
        }
    }

    private suspend fun postOpenAiCompatibleStream(
        url: String,
        headers: Map<String, String>,
        body: JSONObject,
        onPartialReplyText: suspend (String) -> Unit
    ): String {
        val connection = openPostConnection(
            url = url,
            headers = headers,
            readTimeoutMillis = 90_000
        )

        return try {
            connection.outputStream.use { output ->
                output.write(body.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }

            val reader = BufferedReader(InputStreamReader(stream, Charsets.UTF_8))
            val response = try {
                val firstMeaningfulLine = readFirstMeaningfulLine(reader)
                if (firstMeaningfulLine?.trim()?.startsWith("data:") == true) {
                    consumeSseStream(firstMeaningfulLine, reader, onPartialReplyText)
                } else {
                    buildPlainResponse(firstMeaningfulLine, reader)
                }
            } finally {
                reader.close()
            }

            if (responseCode !in 200..299) {
                throw IOException(buildRemoteErrorMessage(responseCode, response))
            }

            response
        } finally {
            connection.disconnect()
        }
    }

    private fun readFirstMeaningfulLine(reader: BufferedReader): String? {
        var pendingLine: String?
        while (true) {
            pendingLine = reader.readLine() ?: return null
            if (pendingLine.isNotBlank()) {
                return pendingLine
            }
        }
    }

    private fun buildPlainResponse(
        firstLine: String?,
        reader: BufferedReader
    ): String {
        val builder = StringBuilder()
        if (firstLine != null) {
            builder.append(firstLine)
        }
        var line = reader.readLine()
        while (line != null) {
            builder.append(line)
            line = reader.readLine()
        }
        return builder.toString()
    }

    private suspend fun consumeSseStream(
        firstLine: String,
        reader: BufferedReader,
        onPartialReplyText: suspend (String) -> Unit
    ): String {
        val fullContent = StringBuilder()
        val eventPayload = StringBuilder()
        var lastPreview = ""

        suspend fun processEventBlock(): Boolean {
            val payload = eventPayload.toString().trim()
            eventPayload.setLength(0)
            if (payload.isBlank()) return false
            if (payload == "[DONE]") return true

            val json = JSONObject(payload)
            val choices = json.optJSONArray("choices") ?: return false
            if (choices.length() == 0) return false

            val firstChoice = choices.optJSONObject(0) ?: return false
            val delta = firstChoice.optJSONObject("delta")
            val chunk = extractOpenAiDeltaText(delta, firstChoice)
            if (chunk.isNotEmpty()) {
                fullContent.append(chunk)
                val preview = extractReplyTextPreview(fullContent.toString())
                if (!preview.isNullOrBlank() && preview != lastPreview) {
                    lastPreview = preview
                    onPartialReplyText(preview)
                }
            }
            return false
        }

        fun appendEventLine(line: String) {
            val trimmed = line.trim()
            if (trimmed.startsWith("data:")) {
                eventPayload.append(trimmed.removePrefix("data:").trim())
            }
        }

        appendEventLine(firstLine)
        var line = reader.readLine()
        while (line != null) {
            if (line.isBlank()) {
                if (processEventBlock()) {
                    break
                }
            } else {
                appendEventLine(line)
            }
            line = reader.readLine()
        }

        if (eventPayload.isNotBlank()) {
            processEventBlock()
        }

        return fullContent.toString()
    }

    private fun extractOpenAiDeltaText(delta: JSONObject?, firstChoice: JSONObject): String {
        delta ?: return firstChoice.optString("text")

        val contentValue = delta.opt("content")
        return when (contentValue) {
            is String -> contentValue
            is JSONArray -> buildString {
                for (index in 0 until contentValue.length()) {
                    val item = contentValue.optJSONObject(index) ?: continue
                    when {
                        item.optString("type") == "text" -> append(item.optString("text"))
                        item.optJSONObject("text") != null -> append(item.optJSONObject("text").optString("value"))
                    }
                }
            }

            else -> ""
        }
    }

    private fun shouldFallbackToNonStreaming(error: Exception): Boolean {
        val message = error.message.orEmpty()
        if (error is IllegalArgumentException) return false
        if (message.contains("HTTP 401")) return false
        if (message.contains("HTTP 403")) return false
        if (message.contains("HTTP 404")) return false

        return error is SocketTimeoutException ||
            error is IOException ||
            message.contains("timeout", ignoreCase = true) ||
            message.contains("stream", ignoreCase = true) ||
            message.contains("JSON", ignoreCase = true)
    }

    private fun openPostConnection(
        url: String,
        headers: Map<String, String>,
        readTimeoutMillis: Int
    ): HttpURLConnection {
        return (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = readTimeoutMillis
            doInput = true
            doOutput = true
            useCaches = false
            headers.forEach { (key, value) -> setRequestProperty(key, value) }
        }
    }

    private fun postJson(
        url: String,
        headers: Map<String, String>,
        body: JSONObject,
        readTimeoutMillis: Int = 45_000
    ): String {
        val connection = openPostConnection(url, headers, readTimeoutMillis)

        return try {
            connection.outputStream.use { output ->
                output.write(body.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream ?: connection.inputStream
            }

            val response = readAll(stream)

            if (responseCode !in 200..299) {
                throw IOException(buildRemoteErrorMessage(responseCode, response))
            }
            response
        } finally {
            connection.disconnect()
        }
    }

    private fun readAll(stream: InputStream): String {
        return BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
            buildString {
                var line = reader.readLine()
                while (line != null) {
                    append(line)
                    line = reader.readLine()
                }
            }
        }
    }

    private fun buildRemoteErrorMessage(responseCode: Int, response: String): String {
        val parsedMessage = runCatching {
            val json = JSONObject(response)
            when {
                json.has("error") && json.get("error") is JSONObject -> {
                    val errorObject = json.getJSONObject("error")
                    listOf(
                        errorObject.optString("type").takeIf { it.isNotBlank() },
                        errorObject.optString("message").takeIf { it.isNotBlank() }
                    ).filterNotNull().joinToString("：")
                }

                json.has("error") && json.optString("error").isNotBlank() -> json.optString("error")
                json.optString("message").isNotBlank() -> json.optString("message")
                else -> response
            }
        }.getOrDefault(response)

        return "连接失败（HTTP $responseCode）：${parsedMessage.take(240)}"
    }

    private fun parseStructuredPayload(rawPayload: String, sourceLabel: String): CompanionStructuredResponse {
        val jsonText = extractJsonObject(rawPayload.trim())
        val json = JSONObject(jsonText)

        val intentType = when (json.optString("intent_type").lowercase()) {
            "expense" -> AiIntentType.EXPENSE
            "income" -> AiIntentType.INCOME
            "clarify" -> AiIntentType.CLARIFY
            else -> AiIntentType.CHAT
        }

        val amount = when {
            json.isNull("amount") -> null
            else -> json.optDouble("amount").takeUnless { it.isNaN() }
        }

        return CompanionStructuredResponse(
            replyText = json.optString("reply_text"),
            intentType = intentType,
            amount = amount,
            category = json.optString("category").takeIf { !json.isNull("category") && it.isNotBlank() },
            remark = json.optString("remark").takeIf { !json.isNull("remark") && it.isNotBlank() },
            needsClarification = json.optBoolean("needs_clarification", false),
            clarificationQuestion = json.optString("clarification_question")
                .takeIf { !json.isNull("clarification_question") && it.isNotBlank() },
            sourceLabel = sourceLabel
        )
    }

    private fun extractJsonObject(rawPayload: String): String {
        if (rawPayload.startsWith("{") && rawPayload.endsWith("}")) {
            return rawPayload
        }

        val cleaned = rawPayload
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        var depth = 0
        var startIndex = -1
        cleaned.forEachIndexed { index, char ->
            when (char) {
                '{' -> {
                    if (depth == 0) startIndex = index
                    depth++
                }

                '}' -> {
                    depth--
                    if (depth == 0 && startIndex >= 0) {
                        return cleaned.substring(startIndex, index + 1)
                    }
                }
            }
        }

        throw IOException("模型返回中未找到合法 JSON：$rawPayload")
    }

    private fun extractReplyTextPreview(rawPayload: String): String? {
        val keyIndex = rawPayload.indexOf("\"reply_text\"")
        if (keyIndex < 0) return null

        val colonIndex = rawPayload.indexOf(':', startIndex = keyIndex)
        if (colonIndex < 0) return null

        var index = colonIndex + 1
        while (index < rawPayload.length && rawPayload[index].isWhitespace()) {
            index++
        }
        if (index >= rawPayload.length || rawPayload[index] != '"') {
            return null
        }

        index++
        val builder = StringBuilder()
        var escaped = false

        while (index < rawPayload.length) {
            val char = rawPayload[index]
            if (escaped) {
                when (char) {
                    '"', '\\', '/' -> builder.append(char)
                    'b' -> builder.append('\b')
                    'f' -> builder.append('\u000C')
                    'n' -> builder.append('\n')
                    'r' -> builder.append('\r')
                    't' -> builder.append('\t')
                    'u' -> {
                        if (index + 4 < rawPayload.length) {
                            val unicode = rawPayload.substring(index + 1, index + 5)
                            unicode.toIntOrNull(16)?.let { code ->
                                builder.append(code.toChar())
                                index += 4
                            } ?: return builder.toString()
                        } else {
                            return builder.toString()
                        }
                    }

                    else -> builder.append(char)
                }
                escaped = false
            } else {
                when (char) {
                    '\\' -> escaped = true
                    '"' -> return builder.toString()
                    else -> builder.append(char)
                }
            }
            index++
        }

        return builder.toString()
    }
}
