package com.colux.libretune.ui.components.song

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.colux.libretune.data.model.Song
import com.colux.libretune.ui.player.PlayerViewModel

@Composable
fun SongItem(
    song: Song, onClick: () -> Unit,
    playerViewModel: PlayerViewModel,
    navController: NavHostController
) {

    val selectedSong by playerViewModel.currentSong.collectAsState()
    val selectedSongIsPlaying by playerViewModel.isPlaying.collectAsState()


    val isSelectedSong = song.id == selectedSong?.id
    val isCurrentlyPlaying = isSelectedSong && selectedSongIsPlaying

    val backgroundColor = if (isSelectedSong) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = Modifier
            .background(backgroundColor)
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(56.dp)) {
            AsyncImage(
                model = song.getBestImageUrl(),
                contentDescription = "Album art for ${song.title}",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            if (isSelectedSong) {
                NowPlayingIndicator(isCurrentlyPlaying)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = song.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
            Text(
                text = song.getArtistNames(),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
    }
}