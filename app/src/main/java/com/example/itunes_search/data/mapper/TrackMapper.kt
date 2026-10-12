package com.example.itunes_search.data.mapper

import com.example.itunes_search.data.model.Track as TrackDto
import com.example.itunes_search.domain.model.Track

fun TrackDto.toDomain(): Track? {
    val id = trackId?.takeIf { it > 0 } ?: return null
    val title = trackName?.takeIf { it.isNotBlank() } ?: return null
    val artist = artistName?.takeIf { it.isNotBlank() } ?: return null
    val mediaKind = kind?.takeIf { it == "song" || it == "music-video" } ?: return null

    return Track(
        trackId = id,
        trackName = title,
        artistName = artist,
        kind = mediaKind,
        collectionId = collectionId,
        collectionName = collectionName,
        primaryGenreName = primaryGenreName,
        trackTimeMillis = trackTimeMillis,
        releaseDate = releaseDate,
        trackPrice = trackPrice,
        currency = currency,
        country = country,
        trackNumber = trackNumber,
        trackCount = trackCount,
        trackExplicitness = trackExplicitness,
        artworkUrl100 = artworkUrl100,
        previewUrl = previewUrl,
        trackViewUrl = trackViewUrl
    )
}
