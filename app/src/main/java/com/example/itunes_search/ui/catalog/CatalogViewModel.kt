package com.example.itunes_search.ui.catalog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.itunes_search.domain.model.TrackResult
import com.example.itunes_search.domain.usecase.SearchTracksUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CatalogViewModel(
    private val searchTracksUseCase: SearchTracksUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val initialQuery = savedStateHandle.get<String>(QUERY_KEY) ?: "Daft Punk"
    private val _uiState = MutableStateFlow(CatalogUiState(query = initialQuery))
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var submittedQuery = initialQuery

    init {
        search()
    }

    fun onQueryChange(query: String) {
        savedStateHandle[QUERY_KEY] = query
        _uiState.update { it.copy(query = query) }
    }

    // Набор текста сам по себе не запускает сеть. Запрос выполняется по кнопке/IME Search.
    fun search() {
        loadTracks(_uiState.value.query)
    }

    fun retry() {
        loadTracks(submittedQuery)
    }

    private fun loadTracks(query: String) {
        val term = query.trim()
        submittedQuery = term
        searchJob?.cancel()
        _uiState.update {
            it.copy(isLoading = true, hasLoaded = false, error = null, tracks = emptyList(), searchedQuery = term)
        }
        searchJob = viewModelScope.launch {
            val result = searchTracksUseCase(term)
            ensureActive()
            when (result) {
                is TrackResult.Success -> _uiState.update {
                    it.copy(isLoading = false, hasLoaded = true, tracks = result.data)
                }
                is TrackResult.Failure -> _uiState.update {
                    it.copy(isLoading = false, error = result.error)
                }
            }
        }
    }

    companion object {
        private const val QUERY_KEY = "search_query"

        fun factory(useCase: SearchTracksUseCase): ViewModelProvider.Factory = viewModelFactory {
            initializer { CatalogViewModel(useCase, createSavedStateHandle()) }
        }
    }
}
