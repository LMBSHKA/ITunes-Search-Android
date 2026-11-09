package com.example.itunes_search

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.itunes_search.data.repository.MockTrackRepository
import com.example.itunes_search.di.AppContainer
import com.example.itunes_search.domain.model.Track
import com.example.itunes_search.domain.model.TrackError
import com.example.itunes_search.domain.model.TrackResult
import com.example.itunes_search.domain.repository.TrackRepository
import com.example.itunes_search.ui.ITunesApp
import com.example.itunes_search.ui.theme.ITunesSearchTheme
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationTest {
    @get:Rule val composeRule = createComposeRule()
    private lateinit var repository: TestRepository

    @Before
    fun setUp() {
        repository = TestRepository()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("track_filters", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("user_profile", Context.MODE_PRIVATE).edit().clear().commit()
        val container = AppContainer(context, repository)
        composeRule.setContent { ITunesSearchTheme { ITunesApp(container) } }
    }

    @Test
    fun bottomNavigationAndDetailsStillWork() {
        composeRule.onNodeWithTag("track_697195462").performClick()
        composeRule.onNodeWithTag("details_track_name").assertTextContains("One More Time")
        composeRule.onNodeWithTag("nav_catalog").assertIsSelected()
        composeRule.onNodeWithTag("details_back").performClick()
        composeRule.onNodeWithTag("track_697195462").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_favorites").performClick()
        composeRule.onNodeWithTag("favorites_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_settings").performClick()
        composeRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_catalog").performClick()
        composeRule.onNodeWithTag("catalog_screen").assertIsDisplayed()
    }

    @Test
    fun savingProfileReturnsToTheTabAndPersistsTheForm() {
        val name = "Иванов Иван Иванович"
        val resumeUrl = "https://example.com/resume.pdf"
        composeRule.onNodeWithTag("nav_profile").performClick()
        composeRule.onNodeWithContentDescription("Редактирование профиля").performClick()
        composeRule.onNodeWithTag("profile_name_input").performTextReplacement(name)
        composeRule.onNodeWithTag("profile_resume_input").performTextReplacement(resumeUrl)
        composeRule.onNodeWithText("Готово").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(hasTestTag("nav_profile")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("nav_profile").assertIsSelected()
        composeRule.onNodeWithText(name).assertIsDisplayed()
        composeRule.onNodeWithText("Резюме").assertIsEnabled()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val reopened = AppContainer(context, repository).profileRepository.profile.value
        assertEquals(name, reopened.fullName)
        assertEquals(resumeUrl, reopened.resumeUrl)
    }

    @Test
    fun submittedSearchSurvivesTabSwitch() {
        composeRule.onNodeWithTag("catalog_search").performTextReplacement("Get Lucky")
        composeRule.onNodeWithTag("catalog_search").performImeAction()
        composeRule.onNodeWithTag("track_617154366").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_settings").performClick()
        composeRule.onNodeWithTag("nav_catalog").performClick()
        composeRule.onNodeWithTag("catalog_search").assertTextContains("Get Lucky")
        composeRule.onNodeWithTag("track_617154366").assertIsDisplayed()
    }

    @Test
    fun listKeepsScrollPositionAfterDetails() {
        composeRule.onNodeWithTag("track_list").performScrollToNode(hasTestTag("track_697195991"))
        composeRule.onNodeWithTag("track_697195991").performClick()
        composeRule.onNodeWithTag("details_track_name").assertTextContains("Voyager")
        composeRule.onNodeWithTag("details_back").performClick()
        composeRule.onNodeWithTag("track_697195991").assertIsDisplayed()
    }

    @Test
    fun emptySearchResultIsDifferentFromAnError() {
        composeRule.onNodeWithTag("catalog_search").performTextReplacement("no-such-track-123")
        composeRule.onNodeWithTag("search_button").performClick()
        composeRule.onNodeWithTag("empty_result").assertIsDisplayed()
    }

    @Test
    fun catalogueShowsLoadingWhileARequestIsPending() {
        val response = CompletableDeferred<TrackResult<List<Track>>>()
        composeRule.runOnIdle { repository.pendingSearch = response }
        composeRule.onNodeWithTag("refresh_button").performClick()
        composeRule.onNodeWithTag("catalog_loading").assertIsDisplayed()
        composeRule.runOnIdle { response.complete(TrackResult.Success(MockTrackRepository.getTracks())) }
        composeRule.onNodeWithTag("track_697195462").assertIsDisplayed()
    }

    @Test
    fun networkErrorShowsRetryAndThenReturnsToTheList() {
        composeRule.runOnIdle { repository.failNextSearch = true }
        composeRule.onNodeWithTag("refresh_button").performClick()
        composeRule.onNodeWithTag("catalog_error").assertIsDisplayed()
        composeRule.onNodeWithTag("retry_button").performClick()
        composeRule.onNodeWithTag("track_697195462").assertIsDisplayed()
    }

    @Test
    fun lookupErrorCanBeRetriedWithoutLeavingTheScreen() {
        composeRule.runOnIdle { repository.failNextLookup = true }
        composeRule.onNodeWithTag("track_697195462").performClick()
        composeRule.onNodeWithTag("details_error").assertIsDisplayed()
        composeRule.onNodeWithTag("retry_button").performClick()
        composeRule.onNodeWithTag("details_track_name").assertTextContains("One More Time")
    }

    // Моки используются только этим тестом, чтобы он не зависел от интернета и каталога Apple.
    private class TestRepository : TrackRepository {
        var failNextSearch = false
        var failNextLookup = false
        var pendingSearch: CompletableDeferred<TrackResult<List<Track>>>? = null

        override suspend fun searchTracks(query: String, limit: Int): TrackResult<List<Track>> {
            if (failNextSearch) {
                failNextSearch = false
                return TrackResult.Failure(TrackError.NETWORK)
            }
            val pending = pendingSearch
            if (pending != null) {
                pendingSearch = null
                return pending.await()
            }
            return MockTrackRepository.searchTracks(query, limit)
        }

        override suspend fun getTrackById(trackId: Long): TrackResult<Track> {
            if (failNextLookup) {
                failNextLookup = false
                return TrackResult.Failure(TrackError.NETWORK)
            }
            return MockTrackRepository.getTrackById(trackId)
        }
    }
}
