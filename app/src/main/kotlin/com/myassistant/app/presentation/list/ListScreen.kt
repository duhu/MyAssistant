package com.myassistant.app.presentation.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.myassistant.app.domain.model.InfoCategory
import com.myassistant.app.presentation.component.CardItem
import com.myassistant.app.presentation.component.CategoryChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    onCardClick: (String) -> Unit,
    viewModel: ListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories = listOf(null) + InfoCategory.entries

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_list)) }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    CategoryChip(
                        label = category?.label ?: stringResource(R.string.category_all),
                        selected = uiState.selectedCategory == category,
                        onClick = { viewModel.handleIntent(ListIntent.SelectCategory(category)) }
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.cards, key = { it.id }) { card ->
                    CardItem(card = card, onClick = { onCardClick(card.id) })
                }
            }
        }
    }
}
