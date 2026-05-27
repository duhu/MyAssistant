package com.myassistant.app.domain.usecase

import com.myassistant.app.domain.model.Card
import com.myassistant.app.domain.model.CardStatus
import com.myassistant.app.domain.repository.CardRepository
import javax.inject.Inject

class UpdateCardUseCase @Inject constructor(
    private val cardRepo: CardRepository
) {
    suspend fun update(card: Card) = cardRepo.updateCard(card.copy(updatedAt = System.currentTimeMillis()))

    suspend fun updateStatus(id: String, status: CardStatus) = cardRepo.updateCardStatus(id, status)

    suspend fun delete(id: String) = cardRepo.deleteCard(id)
}
