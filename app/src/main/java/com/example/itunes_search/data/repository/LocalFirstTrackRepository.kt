package com.example.itunes_search.data.repository

import com.example.itunes_search.domain.model.Track
import com.example.itunes_search.domain.model.TrackResult
import com.example.itunes_search.domain.repository.FavoriteTracksRepository
import com.example.itunes_search.domain.repository.TrackRepository

// Детали сохранённого трека открываются без запроса к iTunes.
class LocalFirstTrackRepository(
    private val remote: TrackRepository,
    private val favorites: FavoriteTracksRepository
) : TrackRepository {
    override suspend fun searchTracks(query: String, limit: Int): TrackResult<List<Track>> =
        remote.searchTracks(query, limit)

    override suspend fun getTrackById(trackId: Long): TrackResult<Track> {
        val savedTrack = favorites.getTrackById(trackId)
        return if (savedTrack != null) TrackResult.Success(savedTrack) else remote.getTrackById(trackId)
    }
}
