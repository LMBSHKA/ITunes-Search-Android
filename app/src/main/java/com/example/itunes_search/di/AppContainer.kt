package com.example.itunes_search.di

import com.example.itunes_search.data.remote.NetworkModule
import com.example.itunes_search.data.repository.NetworkTrackRepository
import com.example.itunes_search.domain.repository.TrackRepository
import com.example.itunes_search.domain.usecase.GetTrackDetailsUseCase
import com.example.itunes_search.domain.usecase.SearchTracksUseCase

// Для небольшой практики зависимости собираются вручную, без DI-фреймворка.
class AppContainer(
    repository: TrackRepository = NetworkTrackRepository(NetworkModule.createApi())
) {
    val searchTracksUseCase = SearchTracksUseCase(repository)
    val getTrackDetailsUseCase = GetTrackDetailsUseCase(repository)
}
