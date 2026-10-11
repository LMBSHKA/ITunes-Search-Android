package com.example.itunes_search.data

import com.example.itunes_search.data.repository.MockTrackRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MockTrackRepositoryTest {
    @Test
    fun snapshotContainsAllProvidedResultsWithUniqueIds() {
        val tracks = MockTrackRepository.getTracks()
        assertEquals(26, tracks.size)
        assertEquals(tracks.size, tracks.map { it.trackId }.toSet().size)
    }

    @Test
    fun lookupMatchesTheProvidedDetailsExample() {
        val track = MockTrackRepository.getTrackById(697195462L)
        assertEquals("One More Time", track?.trackName)
        assertEquals("Discovery", track?.collectionName)
        assertEquals(320357L, track?.trackTimeMillis)
    }

    @Test
    fun unknownIdDoesNotCrash() {
        assertNull(MockTrackRepository.getTrackById(-1L))
    }

    @Test
    fun searchIgnoresCaseAndOuterWhitespace() {
        val result = MockTrackRepository.searchTracks("  gEt LuCkY  ")
        assertEquals(listOf(617154366L), result.map { it.trackId })
    }

    @Test
    fun searchAlsoMatchesArtistAndAlbum() {
        assertTrue(MockTrackRepository.searchTracks("Pentatonix").any { it.trackId == 877651737L })
        val discoveryTracks = MockTrackRepository.searchTracks("discovery")
        assertFalse(discoveryTracks.isEmpty())
        assertTrue(discoveryTracks.all { it.collectionName == "Discovery" })
    }

    @Test
    fun blankSearchReturnsAllResultsAndUnknownTextReturnsEmptyList() {
        assertEquals(MockTrackRepository.getTracks(), MockTrackRepository.searchTracks("  "))
        assertTrue(MockTrackRepository.searchTracks("there-is-no-such-track").isEmpty())
    }

    @Test
    fun videoWithoutAlbumIsPreserved() {
        val track = MockTrackRepository.getTrackById(981695406L)
        assertEquals("music-video", track?.kind)
        assertNull(track?.collectionName)
        assertNull(track?.trackNumber)
    }
}
