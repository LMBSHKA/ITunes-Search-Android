package com.example.itunes_search.domain.model

enum class TrackError {
    EMPTY_QUERY,
    NETWORK,
    TIMEOUT,
    RATE_LIMIT,
    SERVER,
    INVALID_RESPONSE,
    NOT_FOUND,
    UNKNOWN
}
