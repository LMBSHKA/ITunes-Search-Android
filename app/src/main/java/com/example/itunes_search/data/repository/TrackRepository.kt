package com.example.itunes_search.data.repository

import com.example.itunes_search.data.model.Track

interface TrackRepository {
    fun getTracks(): List<Track>
    fun getTrackById(trackId: Long): Track?
    fun searchTracks(query: String): List<Track>
}
