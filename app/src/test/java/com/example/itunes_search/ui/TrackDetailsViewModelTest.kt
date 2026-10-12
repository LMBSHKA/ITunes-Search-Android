package com.example.itunes_search.ui

import androidx.lifecycle.SavedStateHandle
import com.example.itunes_search.domain.model.Track
import com.example.itunes_search.domain.model.TrackError
import com.example.itunes_search.domain.model.TrackResult
import com.example.itunes_search.domain.usecase.GetTrackDetailsUseCase
import com.example.itunes_search.testutil.FakeTrackRepository
import com.example.itunes_search.testutil.MainDispatcherRule
import com.example.itunes_search.ui.details.TrackDetailsViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrackDetailsViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun detailsLoadTheNavigationIdAndShowLoading() = runTest {
        val repository = FakeTrackRepository()
        val response = CompletableDeferred<TrackResult<Track>>()
        repository.detailsHandler = { response.await() }
        val viewModel = TrackDetailsViewModel(
            GetTrackDetailsUseCase(repository), SavedStateHandle(mapOf("trackId" to 697195462L))
        )
        runCurrent()
        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals(listOf(697195462L), repository.requestedIds)
        response.complete(TrackResult.Success(repository.track))
        runCurrent()
        assertEquals(repository.track, viewModel.uiState.value.track)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun failedLookupCanBeRetried() = runTest {
        val repository = FakeTrackRepository().apply { detailsResult = TrackResult.Failure(TrackError.NETWORK) }
        val viewModel = TrackDetailsViewModel(
            GetTrackDetailsUseCase(repository), SavedStateHandle(mapOf("trackId" to 697195462L))
        )
        runCurrent()
        assertEquals(TrackError.NETWORK, viewModel.uiState.value.error)
        repository.detailsResult = TrackResult.Success(repository.track)
        viewModel.retry()
        runCurrent()
        assertEquals(2, repository.requestedIds.size)
        assertEquals(repository.track, viewModel.uiState.value.track)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun missingIdShowsAnErrorWithoutCallingTheRepository() = runTest {
        val repository = FakeTrackRepository()
        val viewModel = TrackDetailsViewModel(GetTrackDetailsUseCase(repository), SavedStateHandle())
        runCurrent()
        assertEquals(TrackError.NOT_FOUND, viewModel.uiState.value.error)
        assertTrue(repository.requestedIds.isEmpty())
    }
}
