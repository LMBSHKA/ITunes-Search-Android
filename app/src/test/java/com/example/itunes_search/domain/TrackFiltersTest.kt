package com.example.itunes_search.domain

import com.example.itunes_search.domain.model.Track
import com.example.itunes_search.domain.model.TrackFilters
import com.example.itunes_search.domain.model.TrackMediaType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackFiltersTest {
    private val track = Track(1, "Track", "Artist", primaryGenreName = "Alternative Rock", trackExplicitness = "explicit")

    @Test fun defaultFiltersKeepEveryTrackAndDoNotNeedBadge() {
        assertFalse(TrackFilters().isActive)
        assertFalse(TrackFilters(genre = "  ").isActive)
        assertTrue(TrackFilters().matches(track))
    }

    @Test fun genreIsCaseInsensitiveAndEmptyGenreMetadataDoesNotMatch() {
        val filters = TrackFilters(genre = " rock ")
        assertTrue(filters.matches(track))
        assertFalse(filters.matches(track.copy(primaryGenreName = null)))
    }

    @Test fun allThreeFiltersAreCombined() {
        val filters = TrackFilters("Rock", TrackMediaType.MUSIC_VIDEO, hideExplicit = true)
        val video = track.copy(kind = "music-video", trackExplicitness = "notExplicit")
        assertTrue(filters.isActive)
        assertTrue(filters.matches(video))
        assertFalse(filters.matches(video.copy(kind = "song")))
        assertFalse(filters.matches(video.copy(primaryGenreName = "Pop")))
        assertFalse(filters.matches(video.copy(trackExplicitness = "explicit")))
    }
}
