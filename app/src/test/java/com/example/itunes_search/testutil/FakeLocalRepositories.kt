package com.example.itunes_search.testutil

import com.example.itunes_search.domain.model.Track
import com.example.itunes_search.domain.model.TrackFilters
import com.example.itunes_search.domain.repository.FavoriteTracksRepository
import com.example.itunes_search.domain.repository.TrackFiltersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTrackFiltersRepository(var filters: TrackFilters = TrackFilters()) : TrackFiltersRepository {
    override fun load() = filters
    override fun save(filters: TrackFilters) { this.filters = filters }
}

class FakeFavoriteTracksRepository : FavoriteTracksRepository {
    private val tracks = MutableStateFlow<List<Track>>(emptyList())
    override fun observeTracks() = tracks
    override fun observeIsFavorite(trackId: Long) = tracks.map { list -> list.any { it.trackId == trackId } }
    override suspend fun getTrackById(trackId: Long) = tracks.value.firstOrNull { it.trackId == trackId }
    override suspend fun add(track: Track) {
        tracks.value = tracks.value.filterNot { it.trackId == track.trackId } + track
    }
    override suspend fun remove(trackId: Long) {
        tracks.value = tracks.value.filterNot { it.trackId == trackId }
    }
}
