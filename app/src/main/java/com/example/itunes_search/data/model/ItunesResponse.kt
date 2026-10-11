package com.example.itunes_search.data.model

// Одинаковая форма ответа для /search и /lookup.
data class ItunesResponse(
    val resultCount: Int,
    val results: List<Track>
)
