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
import com.example.itunes_search.domain.usecase.GetTrackDetailsUseCase
import com.example.itunes_search.ui.navigation.AppRoutes
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrackDetailsViewModel(
    private val getTrackDetailsUseCase: GetTrackDetailsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val trackId = savedStateHandle.get<Long>(AppRoutes.TRACK_ID_ARGUMENT)
    private val _uiState = MutableStateFlow(TrackDetailsUiState())
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        retry()
    }

    fun retry() {
        loadJob?.cancel()
        val id = trackId
        if (id == null || id <= 0) {
            _uiState.value = TrackDetailsUiState(isLoading = false, error = TrackError.NOT_FOUND)
            return
        }
        _uiState.value = TrackDetailsUiState(isLoading = true)
        loadJob = viewModelScope.launch {
            val result = getTrackDetailsUseCase(id)
            ensureActive()
            _uiState.value = when (result) {
                is TrackResult.Success -> TrackDetailsUiState(isLoading = false, track = result.data)
                is TrackResult.Failure -> TrackDetailsUiState(isLoading = false, error = result.error)
            }
        }
    }

    companion object {
        fun factory(useCase: GetTrackDetailsUseCase): ViewModelProvider.Factory = viewModelFactory {
            initializer { TrackDetailsViewModel(useCase, createSavedStateHandle()) }
        }
    }
}
