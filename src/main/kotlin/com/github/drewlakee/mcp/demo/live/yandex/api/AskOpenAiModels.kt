package com.github.drewlakee.mcp.demo.live.yandex.api

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import org.http4k.cloudnative.RemoteRequestFailed
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

import com.fasterxml.jackson.annotation.JsonProperty
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.orThrow

data class OpenAiModelsRequest(
    val model: String,
    val temperature: Float,
    val messages: List<Message>,
) {
    data class Message(
        val role: String,
        val content: List<Content>,
    ) {
        data class Content(
            val type: String,
            val text: String? = null,
            @get:JsonProperty("image_url") val imageUrl: ImageUrl? = null,
        ) {
            data class ImageUrl(val url: String)
        }
    }
}

data class OpenAiModelsResponse(
    val id: String,
    @field:JsonProperty("object") val obj: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage,
) {
    data class Choice(
        val index: Int,
        val message: Message,
        @field:JsonProperty("finish_reason") val finishReason: String,
    ) {
        data class Message(
            val role: String,
            val content: String,
        )
    }

    data class Usage(
        @field:JsonProperty("prompt_tokens") val promptTokens: Int,
        @field:JsonProperty("total_tokens") val totalTokens: Int,
        @field:JsonProperty("completion_tokens") val completionTokens: Int,
    )
}

data class AskOpenAiModelsMessage(
    val role: String,
    val content: List<AskOpenAiModelsMessageContent>,
)

sealed interface AskOpenAiModelsMessageContent {
    fun type(): String
}

data class AskOpenAiModelsTextContent(val text: String): AskOpenAiModelsMessageContent {
    override fun type(): String = "text"
}

data class AskOpenAiModelsImageUrlContent(val url: String): AskOpenAiModelsMessageContent {
    override fun type(): String = "image_url"
}

data class AskOpenAiModels(
    val folderId: String,
    val modelVersion: String,
    val messages: List<AskOpenAiModelsMessage>,
    val temperature: Float? = null,
) : YandexLlmModelsApiAction<OpenAiModelsResponse> {
    override fun toRequest() = Request(Method.POST, "/v1/chat/completions")
        .body(
            OpenAiModelsRequest(
                model = "gpt://$folderId/${modelVersion}",
                temperature = temperature ?: 0.3f,
                messages = messages.map {
                    OpenAiModelsRequest.Message(
                        role = it.role,
                        content = it.content.map {
                            when (it) {
                                is AskOpenAiModelsTextContent -> OpenAiModelsRequest.Message.Content(
                                    type = it.type(),
                                    text = it.text,
                                )
                                is AskOpenAiModelsImageUrlContent -> OpenAiModelsRequest.Message.Content(
                                    type = it.type(),
                                    imageUrl = OpenAiModelsRequest.Message.Content.ImageUrl(it.url)
                                )
                            }
                        },
                    )
                }
            ).let { YandexLlmModelsApiAction.toJsonString(it) }
        )

    override fun toResult(response: Response): Result4k<OpenAiModelsResponse, RemoteRequestFailed> = when(response.status) {
        Status.OK -> Success(YandexLlmModelsApiAction.jsonTo(response.body))
        else -> Failure(RemoteRequestFailed(response.status, response.bodyString()))
    }
}

data class RequestOpenAiModelBuilder(
    private var folderId: String = "",
    private var model: String = "",
    private var temperature: Float = 0.3f,
    private var messages: List<AskOpenAiModelsMessage> = mutableListOf<AskOpenAiModelsMessage>(),
) {

    fun cloudFolder(folderId: () -> String) {
        this.folderId = folderId()
    }

    fun llmModel(model: () -> String) {
        this.model = model()
    }

    fun temperature(temperature: () -> Float) {
        this.temperature = temperature()
    }

    fun userMessage(message: () -> String) {
        messages += AskOpenAiModelsMessage(
            role = "user",
            content = listOf(
                AskOpenAiModelsTextContent(
                    text = message()
                )
            )
        )
    }

    fun systemPromt(promtMessage: () -> String) {
        messages += AskOpenAiModelsMessage(
            role = "system",
            content = listOf(
                AskOpenAiModelsTextContent(
                    text = promtMessage()
                )
            )
        )
    }

    fun build() = AskOpenAiModels(
        folderId = folderId.also {
            if (it.isBlank()) {
                throw IllegalArgumentException("Folder must be set")
            }
        },
        modelVersion = model.also {
            if (it.isBlank()) {
                throw IllegalArgumentException("Model must be set")
            }
        },
        messages = messages.toList(),
        temperature = temperature,
    )
}

fun YandexLlmModelsApi.askOpenAiModel(builderAction: RequestOpenAiModelBuilder.() -> Unit): SimpleLlmResponseMessage =
    invoke(RequestOpenAiModelBuilder().apply(builderAction).build())
        .map { successResult ->
            SimpleLlmResponseMessage(
                message = successResult.choices.asSequence()
                    .map { it.message }
                    .firstOrNull() { it.role == "assistant" }
                    ?.content ?: throw IllegalStateException("LLM Response doesn't contain any assistant content. Response: $successResult")
            )
        }
        .orThrow()