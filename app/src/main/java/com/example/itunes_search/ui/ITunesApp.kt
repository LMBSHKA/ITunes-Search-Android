package com.example.itunes_search.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.itunes_search.R
import com.example.itunes_search.di.AppContainer
import com.example.itunes_search.ui.catalog.CatalogScreen
import com.example.itunes_search.ui.catalog.CatalogViewModel
import com.example.itunes_search.ui.details.TrackDetailsScreen
import com.example.itunes_search.ui.details.TrackDetailsViewModel
import com.example.itunes_search.ui.favorites.FavoritesScreen
import com.example.itunes_search.ui.favorites.FavoritesViewModel
import com.example.itunes_search.ui.navigation.AppRoutes
import com.example.itunes_search.ui.profile.EditProfileScreen
import com.example.itunes_search.ui.profile.EditProfileViewModel
import com.example.itunes_search.ui.profile.ProfileScreen
import com.example.itunes_search.ui.profile.ProfileViewModel
import com.example.itunes_search.ui.settings.SettingsScreen
import com.example.itunes_search.ui.settings.SettingsViewModel

private data class BottomTab(
    val route: String,
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val testTag: String
)

private val bottomTabs = listOf(
    BottomTab(AppRoutes.CATALOG_GRAPH, R.string.tab_catalog, R.drawable.ic_music, "nav_catalog"),
    BottomTab(AppRoutes.FAVORITES, R.string.tab_favorites, R.drawable.ic_favorite, "nav_favorites"),
    BottomTab(AppRoutes.SETTINGS, R.string.tab_settings, R.drawable.ic_settings, "nav_settings"),
    BottomTab(AppRoutes.PROFILE_GRAPH, R.string.tab_profile, R.drawable.ic_profile, "nav_profile")
)

@Composable
fun ITunesApp(container: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    // Каталог общий для вкладок: «Готово» обновляет именно существующий список.
    val catalogFactory = remember(container) {
        CatalogViewModel.factory(container.searchTracksUseCase, container.filtersRepository, container.filterBadgeCache)
    }
    val catalogViewModel: CatalogViewModel = viewModel(factory = catalogFactory)
    val showFilterBadge by catalogViewModel.showFilterBadge.collectAsStateWithLifecycle()
    val openTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (destination?.route != AppRoutes.PROFILE_EDIT) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        val selected = destination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            modifier = Modifier.testTag(tab.testTag),
                            selected = selected,
                            onClick = { if (!selected) openTab(tab.route) },
                            icon = {
                                BadgedBox(badge = {
                                    if (tab.route == AppRoutes.SETTINGS && showFilterBadge) {
                                        Badge(Modifier.testTag("settings_filter_badge"))
                                    }
                                }) {
                                    Icon(painterResource(tab.icon), contentDescription = null)
                                }
                            },
                            label = { Text(stringResource(tab.title)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.CATALOG_GRAPH,
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)
        ) {
            navigation(startDestination = AppRoutes.CATALOG, route = AppRoutes.CATALOG_GRAPH) {
                composable(AppRoutes.CATALOG) {
                    val state by catalogViewModel.uiState.collectAsStateWithLifecycle()
                    CatalogScreen(
                        state = state,
                        onQueryChange = catalogViewModel::onQueryChange,
                        onSearch = catalogViewModel::search,
                        onRetry = catalogViewModel::retry,
                        onTrackClick = { trackId ->
                            navController.navigate(AppRoutes.details(trackId)) { launchSingleTop = true }
                        },
                        showFilterBadge = showFilterBadge,
                        onSettingsClick = { openTab(AppRoutes.SETTINGS) }
                    )
                }
                trackDetailsDestination(AppRoutes.DETAILS, container) { navController.popBackStack() }
            }
            navigation(startDestination = AppRoutes.FAVORITES_LIST, route = AppRoutes.FAVORITES) {
                composable(AppRoutes.FAVORITES_LIST) {
                    val factory = remember(container) { FavoritesViewModel.factory(container.favoriteTracksRepository) }
                    val favoritesViewModel: FavoritesViewModel = viewModel(factory = factory)
                    val state by favoritesViewModel.uiState.collectAsStateWithLifecycle()
                    FavoritesScreen(
                        state = state,
                        onTrackClick = { trackId ->
                            navController.navigate(AppRoutes.favoriteDetails(trackId)) { launchSingleTop = true }
                        },
                        onRetry = favoritesViewModel::retry
                    )
                }
                trackDetailsDestination(AppRoutes.FAVORITE_DETAILS, container) { navController.popBackStack() }
            }
            navigation(startDestination = AppRoutes.PROFILE, route = AppRoutes.PROFILE_GRAPH) {
                composable(AppRoutes.PROFILE) {
                    val factory = remember(container) {
                        ProfileViewModel.factory(container.profileRepository, container.resumeRepository)
                    }
                    val profileViewModel: ProfileViewModel = viewModel(factory = factory)
                    ProfileScreen(profileViewModel, onEdit = {
                        navController.navigate(AppRoutes.PROFILE_EDIT) { launchSingleTop = true }
                    })
                }
                composable(AppRoutes.PROFILE_EDIT) {
                    val factory = remember(container) {
                        EditProfileViewModel.factory(container.profileRepository, container.profilePhotos)
                    }
                    val editProfileViewModel: EditProfileViewModel = viewModel(factory = factory)
                    EditProfileScreen(editProfileViewModel, onBack = { navController.popBackStack() })
                }
            }
            composable(AppRoutes.SETTINGS) {
                val factory = remember(container) {
                    SettingsViewModel.factory(container.filtersRepository, container.filterBadgeCache)
                }
                val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
                val filters by settingsViewModel.filters.collectAsStateWithLifecycle()
                SettingsScreen(
                    filters = filters,
                    onGenreChange = settingsViewModel::onGenreChange,
                    onMediaTypeChange = settingsViewModel::onMediaTypeChange,
                    onHideExplicitChange = settingsViewModel::onHideExplicitChange,
                    onReset = settingsViewModel::reset,
                    onDone = {
                        settingsViewModel.apply()
                        catalogViewModel.retry()
                        // Возвращаемся именно к списку, даже если раньше были открыты детали.
                        navController.popBackStack(AppRoutes.CATALOG, inclusive = false)
                    }
                )
            }
        }
    }
}

private fun NavGraphBuilder.trackDetailsDestination(route: String, container: AppContainer, onBack: () -> Unit) {
    composable(
        route = route,
        arguments = listOf(navArgument(AppRoutes.TRACK_ID_ARGUMENT) { type = NavType.LongType })
    ) {
        val factory = remember(container) {
            TrackDetailsViewModel.factory(container.getTrackDetailsUseCase, container.favoriteTracksRepository)
        }
        val detailsViewModel: TrackDetailsViewModel = viewModel(factory = factory)
        val state by detailsViewModel.uiState.collectAsStateWithLifecycle()
        TrackDetailsScreen(
            state = state,
            onRetry = detailsViewModel::retry,
            onBack = onBack,
            onToggleFavorite = detailsViewModel::toggleFavorite
        )
    }
}
