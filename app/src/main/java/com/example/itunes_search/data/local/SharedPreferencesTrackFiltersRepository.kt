package com.example.itunes_search.data.local

import android.content.SharedPreferences
import com.example.itunes_search.domain.model.TrackFilters
import com.example.itunes_search.domain.model.TrackMediaType
import com.example.itunes_search.domain.repository.TrackFiltersRepository

class SharedPreferencesTrackFiltersRepository(
    private val preferences: SharedPreferences
) : TrackFiltersRepository {
    override fun load(): TrackFilters = TrackFilters(
        genre = preferences.getString("genre", "").orEmpty(),
        mediaType = TrackMediaType.entries.firstOrNull {
            it.name == preferences.getString("media_type", TrackMediaType.ALL.name)
        } ?: TrackMediaType.ALL,
        hideExplicit = preferences.getBoolean("hide_explicit", false)
    )

    override fun save(filters: TrackFilters) {
        preferences.edit()
            .putString("genre", filters.genre.trim())
            .putString("media_type", filters.mediaType.name)
            .putBoolean("hide_explicit", filters.hideExplicit)
            .apply()
    }
}
