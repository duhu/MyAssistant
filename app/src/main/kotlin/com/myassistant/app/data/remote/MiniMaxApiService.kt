package com.myassistant.app.data.remote

import com.myassistant.app.data.remote.dto.MiniMaxVlmRequest
import com.myassistant.app.data.remote.dto.MiniMaxVlmResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface MiniMaxApiService {

    @POST("v1/coding_plan/vlm")
    suspend fun analyzeImage(@Body request: MiniMaxVlmRequest): MiniMaxVlmResponse

    companion object {
        const val BASE_URL_CN = "https://api.minimaxi.com/"
        const val SYSTEM_PROMPT = """你是一个信息提取助手。请分析图片内容，以 JSON 格式返回以下字段：
{
  "title": "简洁的中文标题（≤20字）",
  "category": "日程 | 联系人 | 购物 | 文章 | 代码 | 财务 | 其他",
  "summary": "核心信息摘要（≤100字）",
  "details": "完整解析内容",
  "tags": ["标签1", "标签2"],
  "event_time": "如有时间信息则填写 ISO8601，否则 null",
  "importance": 1-5
}
只返回 JSON，不要额外说明。"""
    }
}
