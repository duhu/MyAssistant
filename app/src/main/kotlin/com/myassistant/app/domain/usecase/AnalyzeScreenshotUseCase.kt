package com.myassistant.app.domain.usecase

import com.myassistant.app.domain.model.Card
import com.myassistant.app.domain.model.CardStatus
import com.myassistant.app.domain.model.InfoCategory
import com.myassistant.app.domain.repository.AIAnalysisRepository
import com.myassistant.app.domain.repository.CardRepository
import java.util.UUID
import javax.inject.Inject

class AnalyzeScreenshotUseCase @Inject constructor(
    private val aiRepo: AIAnalysisRepository,
    private val cardRepo: CardRepository
) {

    suspend operator fun invoke(imagePath: String): Result<Card> = runCatching {
        val analysisResult = aiRepo.analyzeScreenshot(imagePath).getOrThrow()

        val now = System.currentTimeMillis()
        val card = Card(
            id = UUID.randomUUID().toString(),
            title = analysisResult.title,
            category = runCatching { InfoCategory.valueOf(analysisResult.category.uppercase()) }
                .getOrDefault(InfoCategory.OTHER),
            summary = analysisResult.summary,
            details = analysisResult.details,
            tags = analysisResult.tags,
            screenshotPath = imagePath,
            eventTime = analysisResult.eventTime?.let { parseIso8601(it) },
            importance = analysisResult.importance.coerceIn(1, 5),
            createdAt = now,
            updatedAt = now,
            status = CardStatus.NORMAL
        )

        cardRepo.saveCard(card)
        card
    }

    private fun parseIso8601(iso: String): Long? = runCatching {
        java.time.Instant.parse(iso).toEpochMilli()
    }.getOrNull()
}
