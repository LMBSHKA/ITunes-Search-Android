package com.example.itunes_search.data.model

// Сетевая DTO: отсутствующие поля JSON могут быть null. В UI передаётся доменная модель.
data class Track(
    val trackId: Long? = null,
    val trackName: String? = null,
    val artistName: String? = null,
    val kind: String? = null,
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
