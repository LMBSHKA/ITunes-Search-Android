package com.example.itunes_search.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.itunes_search.R
import com.example.itunes_search.ui.catalog.CatalogScreen
import com.example.itunes_search.ui.catalog.CatalogViewModel
import com.example.itunes_search.ui.details.TrackDetailsScreen
import com.example.itunes_search.ui.details.TrackDetailsViewModel
import com.example.itunes_search.ui.navigation.AppRoutes
import com.example.itunes_search.ui.placeholder.PlaceholderScreen

private data class BottomTab(
    val route: String,
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val testTag: String
)

private val bottomTabs = listOf(
    BottomTab(AppRoutes.CATALOG_GRAPH, R.string.tab_catalog, R.drawable.ic_music, "nav_catalog"),
    BottomTab(AppRoutes.FAVORITES, R.string.tab_favorites, R.drawable.ic_favorite, "nav_favorites"),
    BottomTab(AppRoutes.SETTINGS, R.string.tab_settings, R.drawable.ic_settings, "nav_settings")
)

@Composable
fun ITunesApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                bottomTabs.forEach { tab ->
                    val selected = destination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        modifier = Modifier.testTag(tab.testTag),
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                        label = { Text(stringResource(tab.title)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.CATALOG_GRAPH,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            navigation(startDestination = AppRoutes.CATALOG, route = AppRoutes.CATALOG_GRAPH) {
                composable(AppRoutes.CATALOG) {
                    val catalogViewModel: CatalogViewModel = viewModel()
                    CatalogScreen(
                        state = catalogViewModel.uiState,
                        onQueryChange = catalogViewModel::onQueryChange,
                        onTrackClick = { trackId ->
                            navController.navigate(AppRoutes.details(trackId)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(
                    route = AppRoutes.DETAILS,
                    arguments = listOf(navArgument(AppRoutes.TRACK_ID_ARGUMENT) { type = NavType.LongType })
                ) {
                    val detailsViewModel: TrackDetailsViewModel = viewModel()
                    TrackDetailsScreen(
                        track = detailsViewModel.track,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable(AppRoutes.FAVORITES) {
                PlaceholderScreen(
                    title = R.string.tab_favorites,
                    description = R.string.favorites_description,
                    icon = R.drawable.ic_favorite,
                    testTag = "favorites_screen"
                )
            }
            composable(AppRoutes.SETTINGS) {
                PlaceholderScreen(
                    title = R.string.tab_settings,
                    description = R.string.settings_description,
                    icon = R.drawable.ic_settings,
                    testTag = "settings_screen"
                )
            }
        }
    }
}
