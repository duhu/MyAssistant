package com.myassistant.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// MiniMax VLM Request: POST /v1/coding_plan/vlm
@Serializable
data class MiniMaxVlmRequest(
    val prompt: String,
    @SerialName("image_url") val imageUrl: String  // "data:image/jpeg;base64,..."
)

// MiniMax VLM Response
@Serializable
data class MiniMaxVlmResponse(
    val content: String,
    @SerialName("base_resp") val baseResp: MiniMaxBaseResp? = null
)

@Serializable
data class MiniMaxBaseResp(
    @SerialName("status_code") val statusCode: Int = 0,
    @SerialName("status_msg") val statusMsg: String = "success"
)
