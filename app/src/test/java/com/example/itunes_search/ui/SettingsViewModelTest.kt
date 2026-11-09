package com.example.itunes_search.ui

import androidx.lifecycle.SavedStateHandle
import com.example.itunes_search.data.cache.FilterBadgeCache
import com.example.itunes_search.domain.model.TrackFilters
import com.example.itunes_search.domain.model.TrackMediaType
import com.example.itunes_search.testutil.FakeTrackFiltersRepository
import com.example.itunes_search.ui.settings.SettingsViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsViewModelTest {
    @Test fun draftIsAppliedOnlyAfterDoneAndResetClearsTheSharedBadge() {
        val repository = FakeTrackFiltersRepository()
        val cache = FilterBadgeCache(repository.load())
        val viewModel = SettingsViewModel(repository, cache, SavedStateHandle())
        viewModel.onGenreChange(" Rock ")
        viewModel.onMediaTypeChange(TrackMediaType.SONG)
        viewModel.onHideExplicitChange(true)
        assertEquals(TrackFilters(), repository.load())
        assertFalse(cache.isVisible.value)
        viewModel.apply()
        assertEquals(TrackFilters("Rock", TrackMediaType.SONG, true), repository.load())
        assertTrue(cache.isVisible.value)

        val reopened = SettingsViewModel(repository, cache, SavedStateHandle())
        assertEquals(repository.load(), reopened.filters.value)
        reopened.reset()
        assertTrue(cache.isVisible.value)
        reopened.apply()
        assertEquals(TrackFilters(), repository.load())
        assertFalse(cache.isVisible.value)
    }

    @Test fun newCacheReconstructsBadgeFromSavedFilters() {
        val repository = FakeTrackFiltersRepository(TrackFilters(hideExplicit = true))
        val newCache = FilterBadgeCache(repository.load())
        assertTrue(newCache.isVisible.value)
    }
}
