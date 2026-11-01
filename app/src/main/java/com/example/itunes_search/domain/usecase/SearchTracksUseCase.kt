package com.example.itunes_search.domain.usecase

import com.example.itunes_search.domain.model.Track
import com.example.itunes_search.domain.model.TrackError
import com.example.itunes_search.domain.model.TrackResult
import com.example.itunes_search.domain.repository.TrackRepository

class SearchTracksUseCase(private val repository: TrackRepository) {
    suspend operator fun invoke(query: String): TrackResult<List<Track>> {
        val term = query.trim()
        if (term.isEmpty()) return TrackResult.Failure(TrackError.EMPTY_QUERY)
        return repository.searchTracks(query = term, limit = 25)
    }
}
