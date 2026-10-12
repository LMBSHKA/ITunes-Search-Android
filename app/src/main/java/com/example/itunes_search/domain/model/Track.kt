package com.example.itunes_search.domain.model

// Модель приложения. Доменный слой не зависит от Retrofit, Gson и Android.
data class Track(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val kind: String = "song",
    val collectionId: Long? = null,
    val collectionName: String? = null,
    val primaryGenreName: String? = null,
    val trackTimeMillis: Long? = null,
    val releaseDate: String? = null,
    val trackPrice: Double? = null,
    val currency: String? = null,
    val country: String? = null,
    val trackNumber: Int? = null,
    val trackCount: Int? = null,
    val trackExplicitness: String? = null,
    val artworkUrl100: String? = null,
    val previewUrl: String? = null,
    val trackViewUrl: String? = null
)
