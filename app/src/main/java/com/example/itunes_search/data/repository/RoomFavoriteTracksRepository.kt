package com.example.itunes_search.data.repository

import com.example.itunes_search.data.local.FavoriteTrackDao
import com.example.itunes_search.data.local.toDomain
import com.example.itunes_search.data.local.toFavoriteEntity
import com.example.itunes_search.domain.model.Track
import com.example.itunes_search.domain.repository.FavoriteTracksRepository
import kotlinx.coroutines.flow.map

class RoomFavoriteTracksRepository(private val dao: FavoriteTrackDao) : FavoriteTracksRepository {
    override fun observeTracks() = dao.observeAll().map { tracks -> tracks.map { it.toDomain() } }
    override fun observeIsFavorite(trackId: Long) = dao.observeIsFavorite(trackId)
    override suspend fun getTrackById(trackId: Long) = dao.findById(trackId)?.toDomain()
    override suspend fun add(track: Track) = dao.insert(track.toFavoriteEntity())
    override suspend fun remove(trackId: Long) = dao.delete(trackId)
}
