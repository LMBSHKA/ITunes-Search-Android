package com.example.itunes_search.data.model

// Сетевая DTO для /search и /lookup. Null позволяет обнаружить неполный ответ сервера.
data class ItunesResponse(
    val resultCount: Int? = null,
    val results: List<Track?>? = null
)
