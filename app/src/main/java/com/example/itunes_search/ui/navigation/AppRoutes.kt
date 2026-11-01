package com.example.itunes_search.ui.navigation

object AppRoutes {
    const val TRACK_ID_ARGUMENT = "trackId"
    const val CATALOG_GRAPH = "catalog_graph"
    const val CATALOG = "catalog"
    const val FAVORITES = "favorites"
    const val FAVORITES_LIST = "favorites_list"
    const val FAVORITE_DETAILS = "favorites/track/{$TRACK_ID_ARGUMENT}"
    const val SETTINGS = "settings"
    const val DETAILS = "track/{$TRACK_ID_ARGUMENT}"

    fun favoriteDetails(trackId: Long): String = "favorites/track/$trackId"

    fun details(trackId: Long): String = "track/$trackId"
}
