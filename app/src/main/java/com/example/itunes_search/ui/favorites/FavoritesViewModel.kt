package com.example.itunes_search.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.itunes_search.domain.model.Track
import com.example.itunes_search.domain.repository.FavoriteTracksRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val tracks: List<Track> = emptyList(),
    val hasError: Boolean = false
)

class FavoritesViewModel(private val repository: FavoriteTracksRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState = _uiState.asStateFlow()
    private var observeJob: Job? = null

    init { retry() }

    fun retry() {
        observeJob?.cancel()
        _uiState.value = FavoritesUiState()
        observeJob = viewModelScope.launch {
            repository.observeTracks()
                .catch { _uiState.value = FavoritesUiState(isLoading = false, hasError = true) }
                .collect { tracks -> _uiState.value = FavoritesUiState(isLoading = false, tracks = tracks) }
        }
    }

    companion object {
        fun factory(repository: FavoriteTracksRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { FavoritesViewModel(repository) }
        }
    }
}
