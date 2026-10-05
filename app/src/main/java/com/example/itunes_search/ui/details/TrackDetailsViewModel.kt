package com.example.itunes_search.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.itunes_search.data.model.Track
import com.example.itunes_search.data.repository.MockTrackRepository
import com.example.itunes_search.ui.navigation.AppRoutes

class TrackDetailsViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    // Передаём между экранами только ID. Объект берём из общего источника данных.
    private val trackId = savedStateHandle.get<Long>(AppRoutes.TRACK_ID_ARGUMENT)
    val track: Track? = trackId?.let(MockTrackRepository::getTrackById)
}
