package com.example.itunes_search.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.itunes_search.data.cache.FilterBadgeCache
import com.example.itunes_search.domain.model.TrackFilters
import com.example.itunes_search.domain.model.TrackMediaType
import com.example.itunes_search.domain.repository.TrackFiltersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val repository: TrackFiltersRepository,
    private val badgeCache: FilterBadgeCache,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val savedFilters = repository.load()
    private val _filters = MutableStateFlow(
        TrackFilters(
            genre = savedStateHandle[GENRE] ?: savedFilters.genre,
            mediaType = savedStateHandle.get<String>(MEDIA_TYPE)?.let { name ->
                TrackMediaType.entries.firstOrNull { it.name == name }
            } ?: savedFilters.mediaType,
            hideExplicit = savedStateHandle[HIDE_EXPLICIT] ?: savedFilters.hideExplicit
        )
    )
    val filters = _filters.asStateFlow()

    fun onGenreChange(genre: String) = update(_filters.value.copy(genre = genre))
    fun onMediaTypeChange(type: TrackMediaType) = update(_filters.value.copy(mediaType = type))
    fun onHideExplicitChange(hide: Boolean) = update(_filters.value.copy(hideExplicit = hide))
    fun reset() = update(TrackFilters())

    fun apply() {
        val filters = _filters.value.copy(genre = _filters.value.genre.trim())
        repository.save(filters)
        badgeCache.update(filters)
        update(filters)
    }

    private fun update(filters: TrackFilters) {
        _filters.value = filters
        savedStateHandle[GENRE] = filters.genre
        savedStateHandle[MEDIA_TYPE] = filters.mediaType.name
        savedStateHandle[HIDE_EXPLICIT] = filters.hideExplicit
    }

    companion object {
        private const val GENRE = "filter_genre"
        private const val MEDIA_TYPE = "filter_media_type"
        private const val HIDE_EXPLICIT = "filter_hide_explicit"

        fun factory(repository: TrackFiltersRepository, cache: FilterBadgeCache): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { SettingsViewModel(repository, cache, createSavedStateHandle()) }
            }
    }
}
