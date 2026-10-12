package com.example.itunes_search.ui.details

import com.example.itunes_search.domain.model.Track
import com.example.itunes_search.domain.model.TrackError

data class TrackDetailsUiState(
    val isLoading: Boolean = true,
    val track: Track? = null,
    val error: TrackError? = null
)
