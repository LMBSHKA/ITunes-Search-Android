package com.example.itunes_search.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.itunes_search.domain.model.Track

@Entity(tableName = "favorite_tracks")
data class FavoriteTrackEntity(
    @PrimaryKey val trackId: Long,
    val trackName: String,
    val artistName: String,
    val kind: String,
    val collectionId: Long?,
    val collectionName: String?,
    val primaryGenreName: String?,
    val trackTimeMillis: Long?,
    val releaseDate: String?,
    val trackPrice: Double?,
    val currency: String?,
    val country: String?,
    val trackNumber: Int?,
    val trackCount: Int?,
    val trackExplicitness: String?,
    val artworkUrl100: String?,
    val previewUrl: String?,
    val trackViewUrl: String?
)

fun Track.toFavoriteEntity() = FavoriteTrackEntity(
    trackId, trackName, artistName, kind, collectionId, collectionName, primaryGenreName,
    trackTimeMillis, releaseDate, trackPrice, currency, country, trackNumber, trackCount,
    trackExplicitness, artworkUrl100, previewUrl, trackViewUrl
)

fun FavoriteTrackEntity.toDomain() = Track(
    trackId, trackName, artistName, kind, collectionId, collectionName, primaryGenreName,
    trackTimeMillis, releaseDate, trackPrice, currency, country, trackNumber, trackCount,
    trackExplicitness, artworkUrl100, previewUrl, trackViewUrl
)
