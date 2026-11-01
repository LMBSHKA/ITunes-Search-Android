package com.example.itunes_search.di

import android.content.Context
import androidx.room.Room
import com.example.itunes_search.data.cache.FilterBadgeCache
import com.example.itunes_search.data.local.ITunesDatabase
import com.example.itunes_search.data.local.SharedPreferencesTrackFiltersRepository
import com.example.itunes_search.data.remote.NetworkModule
import com.example.itunes_search.data.repository.LocalFirstTrackRepository
import com.example.itunes_search.data.repository.NetworkTrackRepository
import com.example.itunes_search.data.repository.RoomFavoriteTracksRepository
import com.example.itunes_search.domain.repository.FavoriteTracksRepository
import com.example.itunes_search.domain.repository.TrackFiltersRepository
import com.example.itunes_search.domain.repository.TrackRepository
import com.example.itunes_search.domain.usecase.GetTrackDetailsUseCase
import com.example.itunes_search.domain.usecase.SearchTracksUseCase

// Для небольшой практики зависимости собираются вручную, без DI-фреймворка.
class AppContainer(
    context: Context,
    repository: TrackRepository = NetworkTrackRepository(NetworkModule.createApi())
) {
    private val appContext = context.applicationContext
    val filtersRepository: TrackFiltersRepository = SharedPreferencesTrackFiltersRepository(
        appContext.getSharedPreferences("track_filters", Context.MODE_PRIVATE)
    )
    val filterBadgeCache = FilterBadgeCache(filtersRepository.load())

    private val database = Room.databaseBuilder(appContext, ITunesDatabase::class.java, "itunes.db").build()
    val favoriteTracksRepository: FavoriteTracksRepository = RoomFavoriteTracksRepository(database.favoriteTrackDao())

    val searchTracksUseCase = SearchTracksUseCase(repository)
    val getTrackDetailsUseCase = GetTrackDetailsUseCase(LocalFirstTrackRepository(repository, favoriteTracksRepository))
}
