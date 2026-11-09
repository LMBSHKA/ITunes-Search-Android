package com.example.itunes_search

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.itunes_search.data.local.ITunesDatabase
import com.example.itunes_search.data.local.SharedPreferencesTrackFiltersRepository
import com.example.itunes_search.data.repository.RoomFavoriteTracksRepository
import com.example.itunes_search.domain.model.Track
import com.example.itunes_search.domain.model.TrackFilters
import com.example.itunes_search.domain.model.TrackMediaType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalStorageTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val databaseName = "practice5-test.db"
    private var database: ITunesDatabase? = null

    @After fun cleanUp() {
        database?.close()
        context.deleteDatabase(databaseName)
        context.getSharedPreferences("practice5-test-filters", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun completeFavoriteSurvivesDatabaseReopeningWithoutNetwork() = runBlocking {
        context.deleteDatabase(databaseName)
        val track = Track(
            trackId = 1, trackName = "Saved song", artistName = "Artist", kind = "song",
            collectionId = 2, collectionName = "Album", primaryGenreName = "Rock", trackTimeMillis = 123000,
            releaseDate = "2020-01-01T00:00:00Z", trackPrice = 1.29, currency = "USD", country = "USA",
            trackNumber = 3, trackCount = 10, trackExplicitness = "notExplicit", artworkUrl100 = "https://example.com/cover",
            previewUrl = "https://example.com/preview", trackViewUrl = "https://example.com/track"
        )
        database = openDatabase()
        val firstRepository = RoomFavoriteTracksRepository(database!!.favoriteTrackDao())
        firstRepository.add(track)
        firstRepository.add(track)
        assertEquals(1, firstRepository.observeTracks().first().size)
        database!!.close()

        database = openDatabase()
        val reopenedRepository = RoomFavoriteTracksRepository(database!!.favoriteTrackDao())
        assertEquals(track, reopenedRepository.getTrackById(track.trackId))
        assertEquals(listOf(track), reopenedRepository.observeTracks().first())
        assertTrue(reopenedRepository.observeIsFavorite(track.trackId).first())
        reopenedRepository.remove(track.trackId)
        assertTrue(reopenedRepository.observeTracks().first().isEmpty())
        assertFalse(reopenedRepository.observeIsFavorite(track.trackId).first())
    }

    @Test fun filtersAreReadByANewRepositoryInstance() {
        val preferences = context.getSharedPreferences("practice5-test-filters", Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        val filters = TrackFilters("Rock", TrackMediaType.MUSIC_VIDEO, true)
        SharedPreferencesTrackFiltersRepository(preferences).save(filters)
        // commit без изменений дожидается ранее поставленной в очередь записи apply.
        preferences.edit().commit()
        val reopened = SharedPreferencesTrackFiltersRepository(
            context.getSharedPreferences("practice5-test-filters", Context.MODE_PRIVATE)
        )
        assertEquals(filters, reopened.load())
    }

    private fun openDatabase() = Room.databaseBuilder(context, ITunesDatabase::class.java, databaseName).build()
}
