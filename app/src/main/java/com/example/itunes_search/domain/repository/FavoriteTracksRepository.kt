package com.example.itunes_search.domain.repository

import com.example.itunes_search.domain.model.Track
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksRepository {
    fun observeTracks(): Flow<List<Track>>
    fun observeIsFavorite(trackId: Long): Flow<Boolean>
    suspend fun getTrackById(trackId: Long): Track?
    suspend fun add(track: Track)
    suspend fun remove(trackId: Long)
}
