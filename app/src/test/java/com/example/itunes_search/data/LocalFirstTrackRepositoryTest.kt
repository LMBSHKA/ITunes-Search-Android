package com.example.itunes_search.data

import com.example.itunes_search.data.repository.LocalFirstTrackRepository
import com.example.itunes_search.domain.model.TrackError
import com.example.itunes_search.domain.model.TrackResult
import com.example.itunes_search.testutil.FakeFavoriteTracksRepository
import com.example.itunes_search.testutil.FakeTrackRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalFirstTrackRepositoryTest {
    @Test fun savedDetailsDoNotUseNetworkAndRemovalRestoresRemoteLookup() = runTest {
        val remote = FakeTrackRepository().apply { detailsResult = TrackResult.Failure(TrackError.NETWORK) }
        val favorites = FakeFavoriteTracksRepository()
        val repository = LocalFirstTrackRepository(remote, favorites)
        favorites.add(remote.track)
        assertEquals(TrackResult.Success(remote.track), repository.getTrackById(remote.track.trackId))
        assertTrue(remote.requestedIds.isEmpty())
        favorites.remove(remote.track.trackId)
        assertEquals(TrackResult.Failure(TrackError.NETWORK), repository.getTrackById(remote.track.trackId))
        assertEquals(listOf(remote.track.trackId), remote.requestedIds)
    }
}
