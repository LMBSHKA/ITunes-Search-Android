package com.example.itunes_search.domain.usecase

import com.example.itunes_search.domain.model.Track
import com.example.itunes_search.domain.model.TrackError
import com.example.itunes_search.domain.model.TrackResult
import com.example.itunes_search.domain.repository.TrackRepository

class GetTrackDetailsUseCase(private val repository: TrackRepository) {
    suspend operator fun invoke(trackId: Long): TrackResult<Track> {
        if (trackId <= 0) return TrackResult.Failure(TrackError.NOT_FOUND)
        return repository.getTrackById(trackId)
    }
}
