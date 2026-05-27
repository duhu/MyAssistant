package com.myassistant.app.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myassistant.app.domain.model.Card
import com.myassistant.app.domain.usecase.SearchCardsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Card> = emptyList(),
    val isSearching: Boolean = false
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchCardsUseCase: SearchCardsUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _uiState = MutableStateFlow(SearchUiState())

    val uiState: StateFlow<SearchUiState> = _uiState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    init {
        _query
            .debounce(300)
            .flatMapLatest { q ->
                if (q.length < 2) flowOf(emptyList())
                else searchCardsUseCase(q)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
            .also { resultsFlow ->
                viewModelScope.launch {
                    resultsFlow.collect { results ->
                        _uiState.update { it.copy(results = results, isSearching = false) }
                    }
                }
            }
    }

    fun onQueryChange(query: String) {
        _query.value = query
        _uiState.update { it.copy(query = query, isSearching = query.length >= 2) }
    }
}
