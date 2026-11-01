package com.example.itunes_search.data

import com.example.itunes_search.data.repository.MockTrackRepository
import com.example.itunes_search.domain.model.Track
import com.example.itunes_search.domain.model.TrackError
import com.example.itunes_search.domain.model.TrackResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MockTrackRepositoryTest {
    @Test
    fun snapshotStillContainsAllResultsWithUniqueIds() {
        val tracks = MockTrackRepository.getTracks()
        assertEquals(26, tracks.size)
        assertEquals(tracks.size, tracks.map { it.trackId }.toSet().size)
    }

    @Test
    fun lookupMatchesTheOriginalDetailsExample() = runTest {
        val result = MockTrackRepository.getTrackById(697195462L) as TrackResult.Success<Track>
        assertEquals("One More Time", result.data.trackName)
        assertEquals("Discovery", result.data.collectionName)
        assertEquals(320357L, result.data.trackTimeMillis)
    }

    @Test
    fun unknownIdReturnsAnExplicitFailure() = runTest {
        assertEquals(TrackResult.Failure(TrackError.NOT_FOUND), MockTrackRepository.getTrackById(-1L))
    }

    @Test
    fun searchIgnoresCaseAndRespectsTheLimit() = runTest {
        val result = MockTrackRepository.searchTracks("  gEt LuCkY  ", 25) as TrackResult.Success<List<Track>>
        assertEquals(listOf(617154366L), result.data.map { it.trackId })
        val limited = MockTrackRepository.searchTracks("", 2) as TrackResult.Success<List<Track>>
        assertEquals(2, limited.data.size)
    }

    @Test
    fun artistSearchAndVideoWithoutAlbumStillWork() = runTest {
        val result = MockTrackRepository.searchTracks("Pentatonix", 25) as TrackResult.Success<List<Track>>
        assertTrue(result.data.any { it.trackId == 877651737L })
        val video = (MockTrackRepository.getTrackById(981695406L) as TrackResult.Success<Track>).data
        assertEquals("music-video", video.kind)
        assertNull(video.collectionName)
    }
}
