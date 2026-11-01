package com.example.itunes_search.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.itunes_search.R
import com.example.itunes_search.domain.model.TrackFilters
import com.example.itunes_search.domain.model.TrackMediaType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    filters: TrackFilters,
    onGenreChange: (String) -> Unit,
    onMediaTypeChange: (TrackMediaType) -> Unit,
    onHideExplicitChange: (Boolean) -> Unit,
    onReset: () -> Unit,
    onDone: () -> Unit
) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_settings)) }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(16.dp).testTag("settings_screen"),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(stringResource(R.string.filters_description), style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = filters.genre,
                onValueChange = onGenreChange,
                label = { Text(stringResource(R.string.filter_genre)) },
                supportingText = { Text(stringResource(R.string.filter_genre_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("filter_genre")
            )
            Column {
                Text(stringResource(R.string.filter_media_type), style = MaterialTheme.typography.titleMedium)
                // Столбец не обрезается на небольшом экране и при крупном шрифте.
                TrackMediaType.entries.forEach { type ->
                    FilterChip(
                        selected = filters.mediaType == type,
                        onClick = { onMediaTypeChange(type) },
                        label = {
                            Text(stringResource(when (type) {
                                TrackMediaType.ALL -> R.string.media_all
                                TrackMediaType.SONG -> R.string.media_song
                                TrackMediaType.MUSIC_VIDEO -> R.string.media_video
                            }))
                        },
                        modifier = Modifier.testTag("filter_media_${type.name}")
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.filter_hide_explicit), modifier = Modifier.weight(1f))
                Switch(
                    checked = filters.hideExplicit,
                    onCheckedChange = onHideExplicitChange,
                    modifier = Modifier.testTag("filter_hide_explicit")
                )
            }
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth().testTag("filters_done")) {
                Text(stringResource(R.string.done))
            }
            TextButton(onClick = onReset, modifier = Modifier.fillMaxWidth().testTag("filters_reset")) {
                Text(stringResource(R.string.reset_filters))
            }
        }
    }
}
