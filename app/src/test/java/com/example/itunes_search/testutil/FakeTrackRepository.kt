package com.example.itunes_search.testutil

import com.example.itunes_search.domain.model.Track
import com.example.itunes_search.domain.model.TrackResult
import com.example.itunes_search.domain.repository.TrackRepository

class FakeTrackRepository : TrackRepository {
    val track = Track(trackId = 697195462L, trackName = "One More Time", artistName = "Daft Punk")
    val queries = mutableListOf<String>()
    val requestedIds = mutableListOf<Long>()
    var searchResult: TrackResult<List<Track>> = TrackResult.Success(listOf(track))
    var detailsResult: TrackResult<Track> = TrackResult.Success(track)
    var searchHandler: (suspend (String) -> TrackResult<List<Track>>)? = null
    var detailsHandler: (suspend (Long) -> TrackResult<Track>)? = null

    override suspend fun searchTracks(query: String, limit: Int): TrackResult<List<Track>> {
        queries += query
        return searchHandler?.invoke(query) ?: searchResult
    }

    override suspend fun getTrackById(trackId: Long): TrackResult<Track> {
        requestedIds += trackId
        return detailsHandler?.invoke(trackId) ?: detailsResult
    }
}
