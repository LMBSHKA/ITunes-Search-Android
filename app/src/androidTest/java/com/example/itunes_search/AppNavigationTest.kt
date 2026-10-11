package com.example.itunes_search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bottomNavigationSwitchesBetweenThreeTabs() {
        composeRule.onNodeWithTag("catalog_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_favorites").performClick()
        composeRule.onNodeWithTag("favorites_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_favorites").assertIsSelected()
        composeRule.onNodeWithTag("nav_settings").performClick()
        composeRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_catalog").performClick()
        composeRule.onNodeWithTag("catalog_screen").assertIsDisplayed()
    }

    @Test
    fun listOpensTheCorrectDetailsAndReturnsBack() {
        composeRule.onNodeWithTag("track_697195462").performClick()
        composeRule.onNodeWithTag("details_track_name").assertTextContains("One More Time")
        composeRule.onNodeWithTag("nav_catalog").assertIsSelected()
        composeRule.onNodeWithTag("details_back").performClick()
        composeRule.onNodeWithTag("track_697195462").assertIsDisplayed()
    }

    @Test
    fun searchSurvivesTabSwitchAndActivityRecreation() {
        composeRule.onNodeWithTag("catalog_search").performTextInput("Get Lucky")
        composeRule.onNodeWithTag("catalog_search").performImeAction()
        composeRule.onNodeWithTag("track_617154366").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_settings").performClick()
        composeRule.onNodeWithTag("nav_catalog").performClick()
        composeRule.onNodeWithTag("catalog_search").assertTextContains("Get Lucky")
        composeRule.activityRule.scenario.recreate()
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
    fun videoWithoutAlbumOpensAndSystemBackReturnsToList() {
        composeRule.onNodeWithTag("track_list").performScrollToNode(hasTestTag("track_981695406"))
        composeRule.onNodeWithTag("track_981695406").performClick()
        composeRule.onNodeWithTag("track_details").assertIsDisplayed()
        composeRule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeRule.onNodeWithTag("track_981695406").assertIsDisplayed()
    }

    @Test
    fun unknownSearchShowsEmptyState() {
        composeRule.onNodeWithTag("catalog_search").performTextInput("no-such-track-123")
        composeRule.onNodeWithTag("catalog_search").performImeAction()
        composeRule.onNodeWithTag("empty_result").assertIsDisplayed()
    }
}
