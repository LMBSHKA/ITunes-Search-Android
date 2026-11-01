package com.example.itunes_search.domain.model

sealed interface TrackResult<out T> {
    data class Success<T>(val data: T) : TrackResult<T>
    data class Failure(val error: TrackError) : TrackResult<Nothing>
}
