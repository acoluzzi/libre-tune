package com.colux.libretune.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.colux.libretune.ui.components.song.SongMenu
import com.colux.libretune.ui.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerFullScreen(
    playerViewModel: PlayerViewModel,
    navController: NavHostController,
    onDismiss: () -> Unit,
) {
    // 1. Collect all the necessary state directly from the ViewModel.
    val currentSong by playerViewModel.currentSong.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val currentPosition by playerViewModel.currentPosition.collectAsState()
    val totalDuration by playerViewModel.totalDuration.collectAsState()
    val repeatMode by playerViewModel.repeatMode.collectAsState()

    val dynamicColor by playerViewModel.dynamicPrimaryColor.collectAsState()

    var showOptionsMenu by remember { mutableStateOf(false) }


    // This flow depends on the current song's ID.
    // We handle the initial null case for currentSong.
    val isLiked by playerViewModel.isCurrentSongLiked(currentSong?.id ?: "")
        .collectAsState(initial = false)

    var sliderPosition by remember(currentPosition) { mutableFloatStateOf(currentPosition.toFloat()) }
    var isUserSeeking by remember { mutableStateOf(false) }

    val accentColor = dynamicColor ?: MaterialTheme.colorScheme.primary



    currentSong?.let { song ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            // Album Art
            AsyncImage(
                model = song.getBestImageUrl(),
                contentDescription = "Full-screen album art",
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left-aligned Title and Artist
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1, // Must be 1 for marquee to work
                        modifier = Modifier.basicMarquee()
                    )
                    Text(
                        text = song.getArtistNames(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1, // Must be 1 for marquee to work
                        modifier = Modifier.basicMarquee()
                    )
                }
                // Right-aligned Like and More buttons
                IconButton(onClick = { playerViewModel.onLikeClick(song, isLiked) }) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like Song",
                        tint = if (isLiked) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
                IconButton(onClick = { showOptionsMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.White
                    )
                }
            }

            val sliderColors = SliderDefaults.colors().copy(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.4f),
                inactiveTickColor = Color.Transparent,
                activeTickColor = Color.Transparent,
            )

            // Seek Bar (we'll make this functional later)
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = if (isUserSeeking) sliderPosition else currentPosition.toFloat(),
                    onValueChange = {
                        isUserSeeking = true
                        sliderPosition = it
                    },
                    onValueChangeFinished = {
                        playerViewModel.seekToPosition(sliderPosition.toLong())
                        isUserSeeking = false
                    },
                    valueRange = 0f..(totalDuration.toFloat().coerceAtLeast(0f)),
                    modifier = Modifier.fillMaxWidth(),

                    // The thumb composable (smaller size)
                    thumb = {
                        SliderDefaults.Thumb(
                            interactionSource = remember { MutableInteractionSource() },
                            modifier = Modifier
                                .padding(0.dp)
                                .size(16.dp)
                                .background(Color.White, CircleShape),
                            thumbSize = DpSize(16.dp, 16.dp),
                            colors = sliderColors
                        )
                    },

                    // The track composable (thinner)
                    track = { sliderState ->
                        SliderDefaults.Track(
                            sliderState = sliderState,
                            modifier = Modifier.height(4.dp),
                            colors = sliderColors
                        )
                    },

                    colors = sliderColors
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = TimeUtils.formatDuration(if (isUserSeeking) sliderPosition.toLong() else currentPosition),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = TimeUtils.formatDuration(totalDuration),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { // TODO
                }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
                IconButton(onClick = { playerViewModel.playPreviousSong() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowLeft,
                        contentDescription = "Previous",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
                // Bigger, rounded Play/Pause button
                FilledIconButton(
                    onClick = { playerViewModel.onPlayPauseClick() },
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }
                IconButton(onClick = { playerViewModel.playNextSong() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowRight,
                        contentDescription = "Next",
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                }
                IconButton(onClick = { playerViewModel.toggleRepeat() }) {
                    val repeatIcon = when (repeatMode) {
                        Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    }
                    Icon(
                        imageVector = repeatIcon,
                        contentDescription = "Repeat",
                        modifier = Modifier.size(32.dp),
                        tint = if (repeatMode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        if (showOptionsMenu) {
            ModalBottomSheet(onDismissRequest = { showOptionsMenu = false }) {
                SongMenu(
                    song = song,
                    onClose = {
                        showOptionsMenu = false
                        onDismiss()
                    },
                    playerViewModel = playerViewModel,
                    navController = navController,
                )
            }
        }

    }
}


