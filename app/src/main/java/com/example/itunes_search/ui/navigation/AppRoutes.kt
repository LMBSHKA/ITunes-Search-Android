package com.example.itunes_search.ui.navigation

object AppRoutes {
    const val CATALOG_GRAPH = "catalog_graph"
    const val CATALOG = "catalog"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
    const val TRACK_ID_ARGUMENT = "trackId"
    const val DETAILS = "track/{$TRACK_ID_ARGUMENT}"

    fun details(trackId: Long): String = "track/$trackId"
}
