package com.example.itunes_search.data

import com.example.itunes_search.data.model.ItunesResponse
import com.example.itunes_search.data.remote.ItunesApi
import com.example.itunes_search.data.remote.NetworkModule
import com.example.itunes_search.data.repository.NetworkTrackRepository
import com.example.itunes_search.domain.model.Track
import com.example.itunes_search.domain.model.TrackError
import com.example.itunes_search.domain.model.TrackResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class NetworkTrackRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: NetworkTrackRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        repository = NetworkTrackRepository(NetworkModule.createApi(server.url("/").toString()))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun searchEncodesQueryParametersAndMapsTheResponse() = runTest {
        enqueueJson("""{"resultCount":1,"results":[{"trackId":1,"trackName":"Song","artistName":"Artist","kind":"song"}]}""")
        val result = repository.searchTracks("Björk & Daft Punk", 25) as TrackResult.Success<List<Track>>
        assertEquals("Song", result.data.single().trackName)
        assertNull(result.data.single().collectionName)
        val request = server.takeRequest(2, TimeUnit.SECONDS)!!
        assertEquals("GET", request.method)
        assertEquals("/search", request.requestUrl?.encodedPath)
        assertEquals("Björk & Daft Punk", request.requestUrl?.queryParameter("term"))
        assertEquals("25", request.requestUrl?.queryParameter("limit"))
        assertEquals("music", request.requestUrl?.queryParameter("media"))
        assertEquals("US", request.requestUrl?.queryParameter("country"))
    }

    @Test
    fun lookupFindsTheRequestedIdInsteadOfTakingTheFirstResult() = runTest {
        enqueueJson("""{"resultCount":2,"results":[{"trackId":1,"trackName":"Other","artistName":"Artist","kind":"song"},{"trackId":2,"trackName":"Requested","artistName":"Artist","kind":"song"}]}""")
        val result = repository.getTrackById(2L) as TrackResult.Success<Track>
        assertEquals("Requested", result.data.trackName)
        val request = server.takeRequest(2, TimeUnit.SECONDS)!!
        assertEquals("/lookup", request.requestUrl?.encodedPath)
        assertEquals("2", request.requestUrl?.queryParameter("id"))
    }

    @Test
    fun emptySearchIsSuccessButEmptyLookupIsNotFound() = runTest {
        enqueueJson("""{"resultCount":0,"results":[]}""")
        assertEquals(TrackResult.Success(emptyList<Track>()), repository.searchTracks("nothing", 25))
        enqueueJson("""{"resultCount":0,"results":[]}""")
        assertEquals(TrackResult.Failure(TrackError.NOT_FOUND), repository.getTrackById(999L))
    }

    @Test
    fun duplicateAndBrokenEntriesDoNotBreakLazyColumnKeys() = runTest {
        enqueueJson("""{"resultCount":4,"results":[{"trackId":1,"trackName":"Song","artistName":"Artist","kind":"song"},{"trackId":1,"trackName":"Song","artistName":"Artist","kind":"song"},{"trackId":2},null]}""")
        val result = repository.searchTracks("song", 25) as TrackResult.Success<List<Track>>
        assertEquals(listOf(1L), result.data.map { it.trackId })
    }

    @Test
    fun httpErrorsAreExplicitAndDoNotReturnMockData() = runTest {
        server.enqueue(MockResponse().setResponseCode(429))
        assertEquals(TrackResult.Failure(TrackError.RATE_LIMIT), repository.searchTracks("song", 25))
        server.enqueue(MockResponse().setResponseCode(500))
        assertEquals(TrackResult.Failure(TrackError.SERVER), repository.searchTracks("song", 25))
    }

    @Test
    fun invalidJsonAndMissingResultsBecomeResponseErrors() = runTest {
        enqueueJson("not-json")
        assertEquals(TrackResult.Failure(TrackError.INVALID_RESPONSE), repository.searchTracks("song", 25))
        enqueueJson("""{"resultCount":1}""")
        assertEquals(TrackResult.Failure(TrackError.INVALID_RESPONSE), repository.searchTracks("song", 25))
        enqueueJson("""{"resultCount":1,"results":[{"trackId":1}]}""")
        assertEquals(TrackResult.Failure(TrackError.INVALID_RESPONSE), repository.searchTracks("song", 25))
    }

    @Test
    fun networkTimeoutAndCancellationAreHandledSeparately() = runTest {
        val offline = NetworkTrackRepository(apiThrowing(IOException("offline")))
        assertEquals(TrackResult.Failure(TrackError.NETWORK), offline.searchTracks("song", 25))
        val timeout = NetworkTrackRepository(apiThrowing(SocketTimeoutException("timeout")))
        assertEquals(TrackResult.Failure(TrackError.TIMEOUT), timeout.searchTracks("song", 25))
        val cancelled = NetworkTrackRepository(apiThrowing(CancellationException("cancelled")))
        var cancellationThrown = false
        try {
            cancelled.searchTracks("song", 25)
        } catch (_: CancellationException) {
            cancellationThrown = true
        }
        assertTrue(cancellationThrown)
    }

    private fun enqueueJson(json: String) {
        server.enqueue(MockResponse().setResponseCode(200).setHeader("Content-Type", "application/json").setBody(json))
    }

    private fun apiThrowing(exception: Exception): ItunesApi = object : ItunesApi {
        override suspend fun searchTracks(term: String, limit: Int, media: String, country: String): Response<ItunesResponse> =
            throw exception

        override suspend fun getTrackById(trackId: Long, country: String): Response<ItunesResponse> = throw exception
    }
}
