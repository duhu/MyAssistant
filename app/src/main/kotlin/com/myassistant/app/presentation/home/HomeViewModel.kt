package com.myassistant.app.presentation.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myassistant.app.data.util.DiagnosticLog
import com.myassistant.app.data.util.LogEntry
import com.myassistant.app.data.util.SecureStorage
import com.myassistant.app.domain.model.Card
import com.myassistant.app.domain.model.InfoCategory
import com.myassistant.app.domain.usecase.GetCardsUseCase
import com.myassistant.app.service.ScreenshotMonitorService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val recentCards: List<Card> = emptyList(),
    val isMonitorRunning: Boolean = false,
    val isLoading: Boolean = false,
    val showApiKeyWarning: Boolean = false,
    val needNotificationPermission: Boolean = false,
    val error: String? = null,
    val diagnostics: List<LogEntry> = emptyList()
)

sealed class HomeIntent {
    data object ToggleMonitor : HomeIntent()
    data class FilterByCategory(val category: InfoCategory?) : HomeIntent()
    data object DismissError : HomeIntent()
    data object PermissionGranted : HomeIntent()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCardsUseCase: GetCardsUseCase,
    private val secureStorage: SecureStorage,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            isMonitorRunning = secureStorage.isMonitorEnabled(),
            showApiKeyWarning = !secureStorage.hasApiKey(),
            needNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        loadCards()
        collectDiagnostics()
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.ToggleMonitor -> toggleMonitor()
            is HomeIntent.FilterByCategory -> loadCards(intent.category)
            is HomeIntent.DismissError -> _uiState.update { it.copy(error = null) }
            is HomeIntent.PermissionGranted -> {
                _uiState.update { it.copy(needNotificationPermission = false) }
                doStartMonitor()
            }
        }
    }

    fun refreshApiKeyStatus() {
        _uiState.update { it.copy(showApiKeyWarning = !secureStorage.hasApiKey()) }
    }

    fun refreshPermissionStatus() {
        _uiState.update {
            it.copy(
                needNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED
            )
        }
    }

    private fun collectDiagnostics() {
        viewModelScope.launch {
            DiagnosticLog.events.collect { entry ->
                val list = _uiState.value.diagnostics.toMutableList()
                list.add(0, entry)
                _uiState.update { it.copy(diagnostics = list.take(30)) }
            }
        }
    }

    private fun loadCards(category: InfoCategory? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getCardsUseCase(category).collect { cards ->
                _uiState.update {
                    it.copy(
                        recentCards = cards.take(20),
                        isLoading = false,
                        showApiKeyWarning = !secureStorage.hasApiKey()
                    )
                }
            }
        }
    }

    private fun toggleMonitor() {
        if (!secureStorage.hasApiKey()) {
            _uiState.update { it.copy(error = "请先设置 API Key") }
            return
        }

        val newState = !_uiState.value.isMonitorRunning
        if (!newState) {
            // Turning off
            _uiState.update { it.copy(isMonitorRunning = false, error = null) }
            secureStorage.setMonitorEnabled(false)
            context.stopService(Intent(context, ScreenshotMonitorService::class.java))
            return
        }

        // Turning on - check notification permission first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            _uiState.update { it.copy(needNotificationPermission = true) }
            return
        }

        doStartMonitor()
    }

    private fun doStartMonitor() {
        DiagnosticLog.i("正在启动监控服务...")
        _uiState.update { it.copy(isMonitorRunning = true, error = null) }
        secureStorage.setMonitorEnabled(true)
        val intent = Intent(context, ScreenshotMonitorService::class.java)
        try {
            ContextCompat.startForegroundService(context, intent)
        } catch (e: Exception) {
            DiagnosticLog.e("启动服务失败: ${e.message}")
            _uiState.update { it.copy(isMonitorRunning = false, error = "启动失败: ${e.message}") }
            secureStorage.setMonitorEnabled(false)
        }
    }
}
