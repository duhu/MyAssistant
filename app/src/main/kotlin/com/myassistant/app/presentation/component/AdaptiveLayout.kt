package com.myassistant.app.presentation.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AdaptiveCardLayout(
    listContent: @Composable () -> Unit,
    detailContent: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth > 600.dp
        if (isWide) {
            androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxSize()) {
                androidx.compose.foundation.layout.Box(modifier = Modifier.width(320.dp).fillMaxSize()) {
                    listContent()
                }
                androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    detailContent()
                }
            }
        } else {
            listContent()
        }
    }
}
