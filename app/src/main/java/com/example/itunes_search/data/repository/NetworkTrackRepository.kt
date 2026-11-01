package com.example.itunes_search.data.repository

import com.example.itunes_search.data.mapper.toDomain
import com.example.itunes_search.data.model.ItunesResponse
import com.example.itunes_search.data.remote.ItunesApi
import com.example.itunes_search.domain.model.Track
import com.example.itunes_search.domain.model.TrackError
import com.example.itunes_search.domain.model.TrackResult
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.EOFException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException

class NetworkTrackRepository(
    private val api: ItunesApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TrackRepository {
    override suspend fun searchTracks(query: String, limit: Int): TrackResult<List<Track>> =
        executeRequest(
            request = { api.searchTracks(term = query, limit = limit) },
            mapResult = { tracks -> TrackResult.Success(tracks) }
        )

    override suspend fun getTrackById(trackId: Long): TrackResult<Track> =
        executeRequest(
            request = { api.getTrackById(trackId) },
            mapResult = { tracks ->
                val track = tracks.firstOrNull { it.trackId == trackId }
                if (track == null) TrackResult.Failure(TrackError.NOT_FOUND)
                else TrackResult.Success(track)
            }
        )

    private suspend fun <T> executeRequest(
        request: suspend () -> Response<ItunesResponse>,
        mapResult: (List<Track>) -> TrackResult<T>
    ): TrackResult<T> = withContext(ioDispatcher) {
        try {
            val response = request()
            if (!response.isSuccessful) {
                val error = if (response.code() == 429) TrackError.RATE_LIMIT else TrackError.SERVER
                return@withContext TrackResult.Failure(error)
            }

            val body = response.body()
            val results = body?.results
            val count = body?.resultCount
            if (count == null || count < 0 || results == null) {
                return@withContext TrackResult.Failure(TrackError.INVALID_RESPONSE)
            }

            val tracks = results.mapNotNull { it?.toDomain() }.distinctBy { it.trackId }
            if (results.isNotEmpty() && tracks.isEmpty()) {
                return@withContext TrackResult.Failure(TrackError.INVALID_RESPONSE)
            }
            mapResult(tracks)
        } catch (exception: CancellationException) {
            // Отмена старого запроса не должна превращаться в ошибку на экране.
            throw exception
        } catch (_: SocketTimeoutException) {
            TrackResult.Failure(TrackError.TIMEOUT)
        } catch (_: InterruptedIOException) {
            TrackResult.Failure(TrackError.TIMEOUT)
        } catch (_: JsonParseException) {
            TrackResult.Failure(TrackError.INVALID_RESPONSE)
        } catch (_: EOFException) {
            TrackResult.Failure(TrackError.INVALID_RESPONSE)
        } catch (_: IOException) {
            TrackResult.Failure(TrackError.NETWORK)
        } catch (_: Exception) {
            TrackResult.Failure(TrackError.UNKNOWN)
        }
    }
}
