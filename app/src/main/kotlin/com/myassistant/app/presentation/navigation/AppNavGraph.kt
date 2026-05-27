package com.myassistant.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.myassistant.app.R
import com.myassistant.app.presentation.detail.DetailScreen
import com.myassistant.app.presentation.home.HomeScreen
import com.myassistant.app.presentation.list.ListScreen
import com.myassistant.app.presentation.search.SearchScreen
import com.myassistant.app.presentation.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object List : Screen("list")
    data object Search : Screen("search")
    data object Settings : Screen("settings")
    data object Detail : Screen("detail/{cardId}") {
        fun createRoute(cardId: String) = "detail/$cardId"
    }
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val bottomNavItems = listOf(Screen.Home, Screen.List, Screen.Search, Screen.Settings)

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val showBottomBar = bottomNavItems.any { it.route == currentDestination?.route }
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = when (screen) {
                                        Screen.Home -> Icons.Default.Home
                                        Screen.List -> Icons.Default.List
                                        Screen.Search -> Icons.Default.Search
                                        else -> Icons.Default.Settings
                                    },
                                    contentDescription = null
                                )
                            },
                            label = {
                                Text(
                                    text = when (screen) {
                                        Screen.Home -> stringResource(R.string.nav_home)
                                        Screen.List -> stringResource(R.string.nav_list)
                                        Screen.Search -> stringResource(R.string.nav_search)
                                        else -> stringResource(R.string.nav_settings)
                                    }
                                )
                            },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onCardClick = { cardId -> navController.navigate(Screen.Detail.createRoute(cardId)) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.List.route) {
                ListScreen(
                    onCardClick = { cardId -> navController.navigate(Screen.Detail.createRoute(cardId)) }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onCardClick = { cardId -> navController.navigate(Screen.Detail.createRoute(cardId)) }
                )
            }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(Screen.Detail.route) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getString("cardId") ?: return@composable
                DetailScreen(
                    cardId = cardId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
