package com.example.itunes_search.ui

import androidx.lifecycle.SavedStateHandle
import com.example.itunes_search.domain.model.Track
import com.example.itunes_search.domain.model.TrackError
import com.example.itunes_search.domain.model.TrackResult
import com.example.itunes_search.domain.usecase.SearchTracksUseCase
import com.example.itunes_search.testutil.FakeTrackRepository
import com.example.itunes_search.testutil.MainDispatcherRule
import com.example.itunes_search.ui.catalog.CatalogViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialRequestShowsLoadingUntilTheResponseArrives() = runTest {
        val repository = FakeTrackRepository()
        val response = CompletableDeferred<TrackResult<List<Track>>>()
        repository.searchHandler = { response.await() }
        val viewModel = CatalogViewModel(SearchTracksUseCase(repository), SavedStateHandle())
        runCurrent()
        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals(listOf("Daft Punk"), repository.queries)
        response.complete(TrackResult.Success(listOf(repository.track)))
        runCurrent()
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.hasLoaded)
        assertEquals(listOf(repository.track), viewModel.uiState.value.tracks)
    }

    @Test
    fun networkErrorCanBeRetriedAndTypingDoesNotSendRequests() = runTest {
        val repository = FakeTrackRepository().apply { searchResult = TrackResult.Failure(TrackError.NETWORK) }
        val viewModel = CatalogViewModel(SearchTracksUseCase(repository), SavedStateHandle())
        runCurrent()
        assertEquals(TrackError.NETWORK, viewModel.uiState.value.error)
        viewModel.onQueryChange("new draft")
        runCurrent()
        assertEquals(1, repository.queries.size)
        repository.searchResult = TrackResult.Success(listOf(repository.track))
        viewModel.retry()
        runCurrent()
        assertEquals(listOf("Daft Punk", "Daft Punk"), repository.queries)
        assertEquals("new draft", viewModel.uiState.value.query)
        assertEquals(null, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun searchTrimsTheQueryAndDoesNotSendBlankRequests() = runTest {
        val repository = FakeTrackRepository()
        val handle = SavedStateHandle()
        val viewModel = CatalogViewModel(SearchTracksUseCase(repository), handle)
        runCurrent()
        viewModel.onQueryChange("  Get Lucky  ")
        viewModel.search()
        runCurrent()
        assertEquals("Get Lucky", repository.queries.last())
        assertEquals("  Get Lucky  ", handle.get<String>("search_query"))
        viewModel.onQueryChange("   ")
        viewModel.search()
        runCurrent()
        assertEquals(2, repository.queries.size)
        assertEquals(TrackError.EMPTY_QUERY, viewModel.uiState.value.error)
    }

    @Test
    fun cancelledSearchCannotOverwriteANewerResponse() = runTest {
        val repository = FakeTrackRepository()
        val oldResponse = CompletableDeferred<TrackResult<List<Track>>>()
        repository.searchHandler = { query ->
            if (query == "Daft Punk") withContext(NonCancellable) { oldResponse.await() }
            else TrackResult.Success(emptyList())
        }
        val viewModel = CatalogViewModel(SearchTracksUseCase(repository), SavedStateHandle())
        runCurrent()
        viewModel.onQueryChange("new search")
        viewModel.search()
        runCurrent()
        oldResponse.complete(TrackResult.Success(listOf(repository.track)))
        runCurrent()
        assertEquals("new search", viewModel.uiState.value.searchedQuery)
        assertTrue(viewModel.uiState.value.tracks.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
