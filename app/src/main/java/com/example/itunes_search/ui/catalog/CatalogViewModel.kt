package com.example.itunes_search.ui.catalog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.itunes_search.data.repository.MockTrackRepository
import com.example.itunes_search.data.repository.TrackRepository

class CatalogViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val repository: TrackRepository = MockTrackRepository
    private val initialQuery: String = savedStateHandle[QUERY_KEY] ?: ""

    var uiState by mutableStateOf(
        CatalogUiState(
            query = initialQuery,
            tracks = repository.searchTracks(initialQuery),
            totalCount = repository.getTracks().size
        )
    )
        private set

    fun onQueryChange(query: String) {
        savedStateHandle[QUERY_KEY] = query
        uiState = uiState.copy(query = query, tracks = repository.searchTracks(query))
    }

    private companion object {
        const val QUERY_KEY = "search_query"
    }
}
