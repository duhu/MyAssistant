package com.myassistant.app.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.myassistant.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_settings)) }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_api_key)) },
                supportingContent = {
                    Column {
                        OutlinedTextField(
                            value = uiState.apiKey,
                            onValueChange = { viewModel.handleIntent(SettingsIntent.UpdateApiKey(it)) },
                            placeholder = { Text(stringResource(R.string.settings_api_key_hint)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                        TextButton(onClick = { viewModel.handleIntent(SettingsIntent.SaveApiKey) }) {
                            Text(if (uiState.isSaved) "已保存 ✓" else "保存")
                        }
                    }
                }
            )

            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_monitor_switch)) },
                trailingContent = {
                    Switch(
                        checked = uiState.isMonitorEnabled,
                        onCheckedChange = { viewModel.handleIntent(SettingsIntent.ToggleMonitor(it)) }
                    )
                }
            )

            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_auto_analyze)) },
                trailingContent = {
                    Switch(
                        checked = uiState.isAutoAnalyzeEnabled,
                        onCheckedChange = { viewModel.handleIntent(SettingsIntent.ToggleAutoAnalyze(it)) }
                    )
                }
            )
        }
    }
}
