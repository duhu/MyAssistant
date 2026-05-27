package com.myassistant.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myassistant.app.data.util.SecureStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val apiKey: String = "",
    val isMonitorEnabled: Boolean = false,
    val isAutoAnalyzeEnabled: Boolean = true,
    val analyzeQuality: AnalyzeQuality = AnalyzeQuality.ACCURACY,
    val isSaved: Boolean = false
)

enum class AnalyzeQuality { SPEED, ACCURACY }

sealed class SettingsIntent {
    data class UpdateApiKey(val key: String) : SettingsIntent()
    data object SaveApiKey : SettingsIntent()
    data class ToggleMonitor(val enabled: Boolean) : SettingsIntent()
    data class ToggleAutoAnalyze(val enabled: Boolean) : SettingsIntent()
    data class SetAnalyzeQuality(val quality: AnalyzeQuality) : SettingsIntent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val secureStorage: SecureStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            apiKey = secureStorage.getApiKey(),
            isMonitorEnabled = secureStorage.isMonitorEnabled()
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.UpdateApiKey -> _uiState.update { it.copy(apiKey = intent.key, isSaved = false) }
            is SettingsIntent.SaveApiKey -> {
                secureStorage.saveApiKey(_uiState.value.apiKey)
                _uiState.update { it.copy(isSaved = true) }
            }
            is SettingsIntent.ToggleMonitor -> {
                secureStorage.setMonitorEnabled(intent.enabled)
                _uiState.update { it.copy(isMonitorEnabled = intent.enabled) }
            }
            is SettingsIntent.ToggleAutoAnalyze -> _uiState.update { it.copy(isAutoAnalyzeEnabled = intent.enabled) }
            is SettingsIntent.SetAnalyzeQuality -> _uiState.update { it.copy(analyzeQuality = intent.quality) }
        }
    }
}
