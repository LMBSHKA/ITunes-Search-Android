package com.example.itunes_search.data.remote

import com.example.itunes_search.data.model.ItunesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ItunesApi {
    @Headers("Accept: application/json")
    @GET("search")
    suspend fun searchTracks(
        @Query("term") term: String,
        @Query("limit") limit: Int,
        @Query("media") media: String = "music",
        @Query("country") country: String = "US"
    ): Response<ItunesResponse>

    @Headers("Accept: application/json")
    @GET("lookup")
    suspend fun getTrackById(
        @Query("id") trackId: Long,
        @Query("country") country: String = "US"
    ): Response<ItunesResponse>
}
