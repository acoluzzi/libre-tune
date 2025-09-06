package com.colux.libretune.ui.components.song

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import coil.compose.AsyncImage
import com.colux.libretune.data.model.Song
import com.colux.libretune.ui.player.PlayerViewModel

@Composable
fun SongItem(
    song: Song, onClick: () -> Unit,
    playerViewModel: PlayerViewModel,
    onMoreClick: () -> Unit,
) {

    val selectedSong by playerViewModel.currentSong.collectAsState()
    val selectedSongIsPlaying by playerViewModel.isPlaying.collectAsState()


    val isSelectedSong = song.id == selectedSong?.id
    val isCurrentlyPlaying = isSelectedSong && selectedSongIsPlaying

    val backgroundColor = if (isSelectedSong) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val itemShape = RoundedCornerShape(12.dp)

    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(itemShape)
            .background(backgroundColor, shape = itemShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onMoreClick
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
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

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
            Text(
                text = song.getArtistNames(),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
        }

        IconButton(onClick = onMoreClick, modifier = Modifier.width(24.dp)) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options for ${song.title}"
            )
        }
    }
}