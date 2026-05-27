package com.myassistant.app.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myassistant.app.domain.model.Card
import com.myassistant.app.domain.model.InfoCategory
import com.myassistant.app.domain.usecase.GetCardsUseCase
import com.myassistant.app.domain.usecase.UpdateCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListUiState(
    val cards: List<Card> = emptyList(),
    val selectedCategory: InfoCategory? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed class ListIntent {
    data class SelectCategory(val category: InfoCategory?) : ListIntent()
    data class DeleteCard(val cardId: String) : ListIntent()
}

@HiltViewModel
class ListViewModel @Inject constructor(
    private val getCardsUseCase: GetCardsUseCase,
    private val updateCardUseCase: UpdateCardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListUiState())
    val uiState: StateFlow<ListUiState> = _uiState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListUiState())

    init { loadCards(null) }

    fun handleIntent(intent: ListIntent) {
        when (intent) {
            is ListIntent.SelectCategory -> {
                _uiState.update { it.copy(selectedCategory = intent.category) }
                loadCards(intent.category)
            }
            is ListIntent.DeleteCard -> viewModelScope.launch {
                runCatching { updateCardUseCase.delete(intent.cardId) }
            }
        }
    }

    private fun loadCards(category: InfoCategory?) {
        viewModelScope.launch {
            getCardsUseCase(category).collect { cards ->
                _uiState.update { it.copy(cards = cards, isLoading = false) }
            }
        }
    }
}
