package com.example.itunes_search.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.itunes_search.domain.model.TrackError
import com.example.itunes_search.domain.model.TrackResult
import com.example.itunes_search.domain.repository.FavoriteTracksRepository
import com.example.itunes_search.domain.usecase.GetTrackDetailsUseCase
import com.example.itunes_search.ui.navigation.AppRoutes
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrackDetailsViewModel(
    private val getTrackDetailsUseCase: GetTrackDetailsUseCase,
    savedStateHandle: SavedStateHandle,
    private val favoritesRepository: FavoriteTracksRepository
) : ViewModel() {
    private val trackId = savedStateHandle.get<Long>(AppRoutes.TRACK_ID_ARGUMENT)
    private val _uiState = MutableStateFlow(TrackDetailsUiState())
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        retry()
        if (trackId != null && trackId > 0) {
            viewModelScope.launch {
                favoritesRepository.observeIsFavorite(trackId)
                    .catch { _uiState.update { it.copy(favoriteError = true) } }
                    .collect { isFavorite ->
                        _uiState.update { it.copy(isFavorite = isFavorite, isFavoriteLoaded = true) }
                    }
            }
        }
    }

    fun retry() {
        loadJob?.cancel()
        val id = trackId
        if (id == null || id <= 0) {
            _uiState.value = TrackDetailsUiState(isLoading = false, error = TrackError.NOT_FOUND)
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadJob = viewModelScope.launch {
            val result = getTrackDetailsUseCase(id)
            ensureActive()
            _uiState.update { state ->
                when (result) {
                    is TrackResult.Success -> state.copy(isLoading = false, track = result.data, error = null)
                    is TrackResult.Failure -> state.copy(isLoading = false, error = result.error)
                }
            }
        }
    }

    fun toggleFavorite() {
        val state = _uiState.value
        val track = state.track ?: return
        if (!state.isFavoriteLoaded || state.isSavingFavorite) return
        _uiState.update { it.copy(isSavingFavorite = true, favoriteError = false) }
        viewModelScope.launch {
            try {
                if (state.isFavorite) favoritesRepository.remove(track.trackId)
                else favoritesRepository.add(track)
                _uiState.update { it.copy(isFavorite = !state.isFavorite) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _uiState.update { it.copy(favoriteError = true) }
            } finally {
                _uiState.update { it.copy(isSavingFavorite = false) }
            }
        }
    }

    companion object {
        fun factory(useCase: GetTrackDetailsUseCase, favorites: FavoriteTracksRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { TrackDetailsViewModel(useCase, createSavedStateHandle(), favorites) }
        }
    }
}
