package com.example.itunes_search.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.itunes_search.R
import com.example.itunes_search.domain.model.Track

private val artworkColors = listOf(
    listOf(Color(0xFF4F46A5), Color(0xFF9A6BDA)),
    listOf(Color(0xFF125E7D), Color(0xFF31A9A0)),
    listOf(Color(0xFF8B3659), Color(0xFFE28B74)),
    listOf(Color(0xFF285A42), Color(0xFF79A96C))
)

@Composable
fun AlbumArtwork(track: Track, modifier: Modifier = Modifier) {
    // Локальная заглушка: один альбом получает одинаковый цвет на обоих экранах.
    val albumId = track.collectionId ?: track.artistName.hashCode().toLong()
    val colorIndex = ((albumId % artworkColors.size + artworkColors.size) % artworkColors.size).toInt()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(artworkColors[colorIndex])),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.9f, size.height * 0.1f)
            listOf(0.45f, 0.7f, 0.95f).forEach { radius ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.18f),
                    radius = size.width * radius,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
        Icon(
            painter = painterResource(R.drawable.ic_music),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.fillMaxSize(0.5f)
        )
    }
}
