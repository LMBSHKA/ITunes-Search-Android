package com.example.itunes_search.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.itunes_search.R
import com.example.itunes_search.domain.model.TrackError
import com.example.itunes_search.ui.catalog.TrackCard
import com.example.itunes_search.ui.components.ErrorContent
import com.example.itunes_search.ui.components.LoadingContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(state: FavoritesUiState, onTrackClick: (Long) -> Unit, onRetry: () -> Unit) {
    Scaffold(
        modifier = Modifier.testTag("favorites_screen"),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_favorites)) }) }
    ) { padding ->
        when {
            state.isLoading -> LoadingContent(Modifier.fillMaxSize().padding(padding))
            state.hasError -> ErrorContent(TrackError.UNKNOWN, onRetry, Modifier.fillMaxSize().padding(padding))
            state.tracks.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.favorites_empty))
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).testTag("favorites_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.tracks, key = { it.trackId }) { track ->
                    TrackCard(track = track, onClick = { onTrackClick(track.trackId) })
                }
            }
        }
    }
}
