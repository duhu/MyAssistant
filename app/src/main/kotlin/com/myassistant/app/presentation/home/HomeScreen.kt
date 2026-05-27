package com.myassistant.app.presentation.home

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.myassistant.app.R
import com.myassistant.app.presentation.component.CardItem
import com.myassistant.app.presentation.component.MonitorStatusBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCardClick: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.refreshPermissionStatus()
        if (granted) {
            viewModel.handleIntent(HomeIntent.PermissionGranted)
        }
    }

    LaunchedEffect(uiState.needNotificationPermission) {
        if (uiState.needNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MonitorStatusBar(
                isRunning = uiState.isMonitorRunning,
                onToggle = { viewModel.handleIntent(HomeIntent.ToggleMonitor) },
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.showApiKeyWarning) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️ 未配置 API Key", fontSize = 16.sp, color = Color(0xFFE65100))
                        Button(
                            onClick = onNavigateToSettings,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                        ) { Text("去设置", color = Color.White) }
                    }
                }
            }

            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text("❌ $error", modifier = Modifier.padding(8.dp), fontSize = 14.sp, color = Color(0xFFC62828))
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (uiState.recentCards.isEmpty() && !uiState.isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("📸", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("截屏后 AI 自动分析", fontSize = 16.sp, color = Color.Gray)
                        Text("下方面板可查看实时状态", fontSize = 14.sp, color = Color.LightGray)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.recentCards, key = { it.id }) { card ->
                            CardItem(card = card, onClick = { onCardClick(card.id) })
                        }
                    }
                }
            }

            // Diagnostic panel with copy button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E))
            ) {
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF16213E))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📋 诊断日志", color = Color(0xFF00D4FF), fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (uiState.isMonitorRunning) "监控中 · ${uiState.diagnostics.size}条"
                                else "已停止",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                            if (uiState.diagnostics.isNotEmpty()) {
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val text = uiState.diagnostics.joinToString("\n") { "${it.time} ${it.message}" }
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("诊断日志", text))
                                        Toast.makeText(context, "已复制 ${uiState.diagnostics.size} 条日志", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1E88E5)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("📋复制", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                    if (uiState.diagnostics.isEmpty()) {
                        Text(
                            text = if (uiState.isMonitorRunning) "  ⏳ 等待截屏事件..."
                            else "  打开上方开关启动监控",
                            color = Color(0xFF616161),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            items(uiState.diagnostics.take(50)) { entry ->
                                val color = when (entry.level) {
                                    "SUCCESS" -> Color(0xFF4CAF50)
                                    "ERROR" -> Color(0xFFFF5252)
                                    else -> Color(0xFFB0BEC5)
                                }
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        entry.time,
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.width(52.dp)
                                    )
                                    Text(
                                        entry.message,
                                        color = color,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
