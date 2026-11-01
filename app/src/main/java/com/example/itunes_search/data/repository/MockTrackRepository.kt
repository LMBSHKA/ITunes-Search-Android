package com.example.itunes_search.data.repository

import com.example.itunes_search.data.mapper.toDomain
import com.example.itunes_search.domain.model.Track
import com.example.itunes_search.domain.model.TrackError
import com.example.itunes_search.domain.model.TrackResult

// Используется только в тестах. В приложении AppContainer подключает NetworkTrackRepository.
object MockTrackRepository : TrackRepository {
    fun getTracks(): List<Track> = MockTracks.response.results.orEmpty().mapNotNull { it?.toDomain() }

    override suspend fun getTrackById(trackId: Long): TrackResult<Track> {
        val track = getTracks().firstOrNull { it.trackId == trackId }
        return if (track == null) TrackResult.Failure(TrackError.NOT_FOUND)
        else TrackResult.Success(track)
    }

    override suspend fun searchTracks(query: String, limit: Int): TrackResult<List<Track>> {
        val searchText = query.trim()
        val tracks = if (searchText.isEmpty()) getTracks() else getTracks().filter { track ->
            track.trackName.contains(searchText, ignoreCase = true) ||
                track.artistName.contains(searchText, ignoreCase = true) ||
                track.collectionName.orEmpty().contains(searchText, ignoreCase = true)
        }
        return TrackResult.Success(tracks.take(limit))
    }
}
