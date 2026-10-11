package com.example.itunes_search.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.itunes_search.R
import com.example.itunes_search.data.model.Track
import com.example.itunes_search.ui.components.AlbumArtwork
import com.example.itunes_search.util.formatDuration
import com.example.itunes_search.util.formatPrice
import com.example.itunes_search.util.formatReleaseDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackDetailsScreen(track: Track?, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("details_back")) {
                        Icon(
                            painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (track == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.track_not_found))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .testTag("track_details"),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                TrackHeader(track)
                TrackInformation(track)
            }
        }
    }
}

@Composable
private fun TrackHeader(track: Track) {
    ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
        val (artwork, title, artist, mediaType, divider, statistics) = createRefs()
        val headerBottom = createBottomBarrier(artwork, mediaType)

        AlbumArtwork(
            track = track,
            modifier = Modifier.size(104.dp).constrainAs(artwork) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            }
        )
        Text(
            text = track.trackName,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.testTag("details_track_name").constrainAs(title) {
                top.linkTo(parent.top)
                start.linkTo(artwork.end, margin = 16.dp)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )
        Text(
            text = track.artistName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.constrainAs(artist) {
                top.linkTo(title.bottom, margin = 8.dp)
                start.linkTo(title.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.constrainAs(mediaType) {
                top.linkTo(artist.bottom, margin = 12.dp)
                start.linkTo(title.start)
                end.linkTo(parent.end)
                horizontalBias = 0f
                width = Dimension.preferredWrapContent
            }
        ) {
            Text(
                text = stringResource(if (track.kind == "music-video") R.string.media_video else R.string.media_song),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        HorizontalDivider(
            modifier = Modifier.constrainAs(divider) {
                top.linkTo(headerBottom, margin = 24.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )
        Row(
            modifier = Modifier.constrainAs(statistics) {
                top.linkTo(divider.bottom, margin = 16.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            },
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.duration),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(formatDuration(track.trackTimeMillis), style = MaterialTheme.typography.titleLarge)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.price),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(formatPrice(track.trackPrice, track.currency), style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun TrackInformation(track: Track) {
    Column {
        Text(stringResource(R.string.about_track), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoField(stringResource(R.string.album), track.collectionName ?: stringResource(R.string.unknown_album))
                HorizontalDivider()
                InfoField(stringResource(R.string.genre), track.primaryGenreName ?: "—")
                InfoField(stringResource(R.string.release_date), formatReleaseDate(track.releaseDate))
                InfoField(stringResource(R.string.country), track.country ?: "—")
                if (track.trackNumber != null) {
                    val number = if (track.trackCount != null) {
                        stringResource(R.string.track_number_value, track.trackNumber, track.trackCount)
                    } else {
                        track.trackNumber.toString()
                    }
                    InfoField(stringResource(R.string.track_number), number)
                }
                val explicitness = when (track.trackExplicitness) {
                    "explicit" -> R.string.explicit_content
                    "notExplicit" -> R.string.no_explicit_content
                    "cleaned" -> R.string.cleaned_content
                    else -> null
                }
                if (explicitness != null) {
                    InfoField(stringResource(R.string.content_label), stringResource(explicitness))
                }
            }
        }
    }
}

@Composable
private fun InfoField(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
