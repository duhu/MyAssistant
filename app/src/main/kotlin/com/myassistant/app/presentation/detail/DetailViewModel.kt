package com.myassistant.app.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myassistant.app.domain.model.Card
import com.myassistant.app.domain.model.CardStatus
import com.myassistant.app.domain.repository.CardRepository
import com.myassistant.app.domain.usecase.UpdateCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val card: Card? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed class DetailIntent {
    data class Archive(val cardId: String) : DetailIntent()
    data class Delete(val cardId: String) : DetailIntent()
    data class UpdateCard(val card: Card) : DetailIntent()
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: CardRepository,
    private val updateCardUseCase: UpdateCardUseCase
) : ViewModel() {

    private val cardId: String = checkNotNull(savedStateHandle["cardId"])

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState())

    init {
        viewModelScope.launch {
            val card = runCatching { cardRepository.getCardById(cardId) }.getOrNull()
            _uiState.update { it.copy(card = card, isLoading = false) }
        }
    }

    fun handleIntent(intent: DetailIntent) {
        when (intent) {
            is DetailIntent.Archive -> viewModelScope.launch {
                runCatching { updateCardUseCase.updateStatus(intent.cardId, CardStatus.ARCHIVED) }
            }
            is DetailIntent.Delete -> viewModelScope.launch {
                runCatching { updateCardUseCase.delete(intent.cardId) }
            }
            is DetailIntent.UpdateCard -> viewModelScope.launch {
                runCatching { updateCardUseCase.update(intent.card) }
                    .onSuccess { _uiState.update { it.copy(card = intent.card) } }
            }
        }
    }
}
