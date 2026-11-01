package com.example.itunes_search.domain.model

enum class TrackMediaType { ALL, SONG, MUSIC_VIDEO }

data class TrackFilters(
    val genre: String = "",
    val mediaType: TrackMediaType = TrackMediaType.ALL,
    val hideExplicit: Boolean = false
) {
    val isActive: Boolean
        get() = genre.isNotBlank() || mediaType != TrackMediaType.ALL || hideExplicit

    fun matches(track: Track): Boolean =
        (genre.isBlank() || track.primaryGenreName?.contains(genre.trim(), ignoreCase = true) == true) &&
            (mediaType == TrackMediaType.ALL ||
                (mediaType == TrackMediaType.SONG && track.kind == "song") ||
                (mediaType == TrackMediaType.MUSIC_VIDEO && track.kind == "music-video")) &&
            (!hideExplicit || track.trackExplicitness != "explicit")
}
