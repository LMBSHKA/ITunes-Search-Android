package com.example.itunes_search.ui.catalog

import com.example.itunes_search.data.model.Track

data class CatalogUiState(
    val query: String = "",
    val tracks: List<Track> = emptyList(),
    val totalCount: Int = 0
)
