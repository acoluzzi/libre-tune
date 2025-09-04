package com.colux.libretune.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import coil.compose.AsyncImage
import com.colux.libretune.data.model.Song

@Composable
fun PlayerBar(
    song: Song,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier, currentPosition: Long,
    totalDuration: Long,
    dynamicColor: Color
) {
    val progress = if (totalDuration > 0) {
        currentPosition.toFloat() / totalDuration.toFloat()
    } else {
        0f
    }
    val contentColor = if (dynamicColor.isLight()) Color.Black else Color.White

    Column(
        modifier = modifier.clip(
            RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomEnd = 12.dp,
                bottomStart = 12.dp
            )
        )
    ) {


        Row(
            // This modifier is now just for the Row's specific styling
            modifier = Modifier
                .fillMaxWidth()
                .background(dynamicColor) // This will be your dark gray
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.images.firstOrNull()?.url,
                contentDescription = "Song thumbnail",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    color = contentColor
                )
                Text(
                    song.getArtistNames(),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    color = contentColor
                )
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onPlayPauseClick) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    modifier = Modifier.size(32.dp),
                    contentColor
                )
            }
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp),
            // Explicitly set the color to White, if you want it always white
            color = Color.White, // Progress color is always white
            trackColor = Color.White.copy(alpha = 0.3f)
        )
    }
}

fun Color.isLight(): Boolean {
    return ColorUtils.calculateLuminance(this.toArgb()) > 0.5
}