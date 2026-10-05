package com.example.itunes_search.data.repository

import com.example.itunes_search.data.model.Track

// Пока работаем с локальными данными. Обращения к API будут в следующей практике.
object MockTrackRepository : TrackRepository {
    override fun getTracks(): List<Track> = MockTracks.response.results

    override fun getTrackById(trackId: Long): Track? =
        getTracks().firstOrNull { it.trackId == trackId }

    override fun searchTracks(query: String): List<Track> {
        val searchText = query.trim()
        if (searchText.isEmpty()) return getTracks()

        return getTracks().filter { track ->
            track.trackName.contains(searchText, ignoreCase = true) ||
                track.artistName.contains(searchText, ignoreCase = true) ||
                track.collectionName.orEmpty().contains(searchText, ignoreCase = true)
        }
    }
}
