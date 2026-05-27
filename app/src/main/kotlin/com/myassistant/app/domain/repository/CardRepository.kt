package com.myassistant.app.domain.repository

import com.myassistant.app.domain.model.Card
import com.myassistant.app.domain.model.CardStatus
import com.myassistant.app.domain.model.InfoCategory
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    fun getAllCards(): Flow<List<Card>>
    fun getCardsByCategory(category: InfoCategory): Flow<List<Card>>
    fun searchCards(query: String): Flow<List<Card>>
    suspend fun getCardById(id: String): Card?
    suspend fun saveCard(card: Card)
    suspend fun updateCard(card: Card)
    suspend fun updateCardStatus(id: String, status: CardStatus)
    suspend fun deleteCard(id: String)
}
