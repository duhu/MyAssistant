package com.myassistant.app.data.repository

import com.myassistant.app.data.remote.MiniMaxApiService
import com.myassistant.app.data.remote.dto.AnalysisResult
import com.myassistant.app.data.remote.dto.MiniMaxVlmRequest
import com.myassistant.app.data.util.DiagnosticLog
import com.myassistant.app.data.util.ImageCompressor
import com.myassistant.app.data.util.SecureStorage
import com.myassistant.app.domain.repository.AIAnalysisRepository
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIAnalysisRepositoryImpl @Inject constructor(
    private val api: MiniMaxApiService,
    private val imageCompressor: ImageCompressor,
    private val secureStorage: SecureStorage
) : AIAnalysisRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun analyzeScreenshot(imagePath: String): Result<AnalysisResult> =
        runCatching {
            val apiKey = secureStorage.getApiKey()
            if (apiKey.isBlank()) {
                error("MiniMax API Key 未配置")
            }

            // Compress image to base64
            DiagnosticLog.i("压缩图片...")
            val base64 = imageCompressor.compressToBase64(imagePath).getOrThrow()
            if (base64.isBlank()) error("压缩结果为空")
            DiagnosticLog.ok("压缩完成: ${base64.length / 1024}KB")

            // Call MiniMax VLM endpoint
            DiagnosticLog.i("调用 MiniMax VLM (${base64.length / 1024}KB)...")
            val request = MiniMaxVlmRequest(
                prompt = MiniMaxApiService.SYSTEM_PROMPT + "\n请分析这张截图的内容",
                imageUrl = "data:image/jpeg;base64,$base64"
            )

            val response = api.analyzeImage(request)
            if (response.baseResp?.statusCode != 0 && response.baseResp?.statusCode != null) {
                error("MiniMax 错误: ${response.baseResp.statusMsg} (${response.baseResp.statusCode})")
            }

            val rawContent = response.content
            if (rawContent.isBlank()) error("MiniMax 返回空")
            DiagnosticLog.i("VLM 返回 (${rawContent.length} chars)")

            val cleanJson = rawContent.trim()
                .removePrefix("```json").removeSuffix("```").trim()
            json.decodeFromString<AnalysisResult>(cleanJson)
        }.onFailure { e ->
            val detail = when (e) {
                is HttpException -> {
                    val body = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
                    "HTTP ${e.code()}: ${body?.take(200) ?: e.message()}"
                }
                else -> e.message ?: "未知错误"
            }
            DiagnosticLog.e("MiniMax错误: $detail")
        }

    companion object {
        const val TAG = "MiniMaxVLM"
    }
}
