package com.myassistant.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VisionRequest(
    val model: String = "deepseek-chat",
    val messages: List<VisionMessage>,
    @SerialName("max_tokens") val maxTokens: Int = 1000,
    val temperature: Double = 0.3
)

@Serializable
data class VisionMessage(
    val role: String,
    val content: List<VisionContent>
)

@Serializable
data class VisionContent(
    val type: String,
    val text: String? = null,
    @SerialName("image_url") val imageUrl: VisionImageUrl? = null
)

@Serializable
data class VisionImageUrl(
    val url: String
)

// Response DTOs
@Serializable
data class VisionResponse(
    val id: String,
    val model: String,
    val choices: List<VisionChoice>,
    val usage: VisionUsage? = null
)

@Serializable
data class VisionChoice(
    val index: Int,
    val message: VisionResponseMessage,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class VisionResponseMessage(
    val role: String,
    val content: String
)

@Serializable
data class VisionUsage(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("completion_tokens") val completionTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int
)

@Serializable
data class AnalysisResult(
    val title: String,
    val category: String,
    val summary: String,
    val details: String,
    val tags: List<String>,
    @SerialName("event_time") val eventTime: String? = null,
    val importance: Int = 3
)
