package com.myassistant.app.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.myassistant.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    cardId: String,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val card = uiState.card

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(card?.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: edit */ }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit))
                    }
                }
            )
        }
    ) { innerPadding ->
        if (card == null) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(text = "分类：${card.category.label}", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "摘要", style = MaterialTheme.typography.titleSmall)
            Text(text = card.summary)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "详细信息", style = MaterialTheme.typography.titleSmall)
            Text(text = card.details)
            Spacer(modifier = Modifier.height(12.dp))
            if (card.tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    card.tags.forEach { tag -> AssistChip(onClick = {}, label = { Text(tag) }) }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = { /* TODO: reanalyze */ }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.action_reanalyze))
                }
                OutlinedButton(
                    onClick = { viewModel.handleIntent(DetailIntent.Archive(card.id)); onBack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.action_archive))
                }
                IconButton(onClick = { viewModel.handleIntent(DetailIntent.Delete(card.id)); onBack() }) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
                }
            }
        }
    }
}
