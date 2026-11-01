package com.example.itunes_search.domain.repository

import com.example.itunes_search.domain.model.Track
import com.example.itunes_search.domain.model.TrackResult

interface TrackRepository {
    suspend fun searchTracks(query: String, limit: Int = 25): TrackResult<List<Track>>
    suspend fun getTrackById(trackId: Long): TrackResult<Track>
}
