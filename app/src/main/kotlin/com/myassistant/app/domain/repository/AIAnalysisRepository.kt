package com.myassistant.app.domain.repository

import com.myassistant.app.data.remote.dto.AnalysisResult

interface AIAnalysisRepository {
    suspend fun analyzeScreenshot(imagePath: String): Result<AnalysisResult>
}
