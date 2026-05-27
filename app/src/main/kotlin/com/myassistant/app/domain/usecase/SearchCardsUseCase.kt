package com.myassistant.app.domain.usecase

import com.myassistant.app.domain.model.Card
import com.myassistant.app.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchCardsUseCase @Inject constructor(
    private val cardRepo: CardRepository
) {
    operator fun invoke(query: String): Flow<List<Card>> = cardRepo.searchCards(query)
}
