package com.example.itunes_search.domain.repository

import com.example.itunes_search.domain.model.TrackFilters

interface TrackFiltersRepository {
    fun load(): TrackFilters
    fun save(filters: TrackFilters)
}
