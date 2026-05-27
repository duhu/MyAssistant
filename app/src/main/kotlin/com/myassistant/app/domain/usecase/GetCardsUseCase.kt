package com.myassistant.app.domain.usecase

import com.myassistant.app.domain.model.Card
import com.myassistant.app.domain.model.InfoCategory
import com.myassistant.app.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCardsUseCase @Inject constructor(
    private val cardRepo: CardRepository
) {
    operator fun invoke(category: InfoCategory? = null): Flow<List<Card>> =
        if (category == null) cardRepo.getAllCards()
        else cardRepo.getCardsByCategory(category)
}
