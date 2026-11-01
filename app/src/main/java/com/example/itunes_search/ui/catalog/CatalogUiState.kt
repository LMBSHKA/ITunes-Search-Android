package com.example.itunes_search.ui.catalog

import com.example.itunes_search.domain.model.Track
import com.example.itunes_search.domain.model.TrackError

data class CatalogUiState(
    val query: String = "Daft Punk",
    val tracks: List<Track> = emptyList(),
    val searchedQuery: String = "",
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val error: TrackError? = null
)
