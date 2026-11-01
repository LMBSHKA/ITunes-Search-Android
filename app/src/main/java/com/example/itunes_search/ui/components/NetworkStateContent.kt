package com.example.itunes_search.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.itunes_search.R
import com.example.itunes_search.domain.model.TrackError

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        CircularProgressIndicator()
        Text(stringResource(R.string.loading), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ErrorContent(error: TrackError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(stringResource(R.string.loading_failed), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(error.messageResource()),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry, modifier = Modifier.testTag("retry_button")) {
            Text(stringResource(R.string.retry))
        }
    }
}

@StringRes
private fun TrackError.messageResource(): Int = when (this) {
    TrackError.EMPTY_QUERY -> R.string.error_empty_query
    TrackError.NETWORK -> R.string.error_network
    TrackError.TIMEOUT -> R.string.error_timeout
    TrackError.RATE_LIMIT -> R.string.error_rate_limit
    TrackError.SERVER -> R.string.error_server
    TrackError.INVALID_RESPONSE -> R.string.error_invalid_response
    TrackError.NOT_FOUND -> R.string.track_not_found
    TrackError.UNKNOWN -> R.string.error_unknown
}
