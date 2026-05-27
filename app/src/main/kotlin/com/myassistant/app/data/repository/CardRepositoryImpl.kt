package com.myassistant.app.data.repository

import com.myassistant.app.data.local.dao.CardDao
import com.myassistant.app.data.local.entity.CardEntity
import com.myassistant.app.domain.model.Card
import com.myassistant.app.domain.model.CardStatus
import com.myassistant.app.domain.model.InfoCategory
import com.myassistant.app.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class CardRepositoryImpl @Inject constructor(
    private val cardDao: CardDao
) : CardRepository {

    override fun getAllCards(): Flow<List<Card>> =
        cardDao.getAllCards().map { list -> list.map { it.toDomain() } }

    override fun getCardsByCategory(category: InfoCategory): Flow<List<Card>> =
        cardDao.getCardsByCategory(category.name).map { list -> list.map { it.toDomain() } }

    override fun searchCards(query: String): Flow<List<Card>> =
        cardDao.searchCards("$query*").map { list -> list.map { it.toDomain() } }

    override suspend fun getCardById(id: String): Card? =
        cardDao.getCardById(id)?.toDomain()

    override suspend fun saveCard(card: Card) =
        cardDao.insertCard(card.toEntity())

    override suspend fun updateCard(card: Card) =
        cardDao.updateCard(card.toEntity())

    override suspend fun updateCardStatus(id: String, status: CardStatus) =
        cardDao.updateCardStatus(id, status.name, System.currentTimeMillis())

    override suspend fun deleteCard(id: String) =
        cardDao.deleteCard(id)

    private fun CardEntity.toDomain() = Card(
        id = id,
        title = title,
        category = runCatching { InfoCategory.valueOf(category) }.getOrDefault(InfoCategory.OTHER),
        summary = summary,
        details = details,
        tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        screenshotPath = screenshotPath,
        eventTime = eventTime,
        importance = importance,
        createdAt = createdAt,
        updatedAt = updatedAt,
        status = runCatching { CardStatus.valueOf(status) }.getOrDefault(CardStatus.NORMAL)
    )

    private fun Card.toEntity(): CardEntity {
        val tagsJson = tags.joinToString(",")
        return CardEntity(
        id = id.ifBlank { UUID.randomUUID().toString() },
        title = title,
        category = category.name,
        summary = summary,
        details = details,
        tags = tagsJson,
        screenshotPath = screenshotPath,
        eventTime = eventTime,
        importance = importance,
        createdAt = createdAt,
        updatedAt = updatedAt,
        status = status.name
    )
    }
}
