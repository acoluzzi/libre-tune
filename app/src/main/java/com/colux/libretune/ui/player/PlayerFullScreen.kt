package com.colux.libretune.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAddCircle
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.colux.libretune.data.model.Song
import com.colux.libretune.ui.nav.Screen
import com.colux.libretune.ui.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerFullScreen(
    playerViewModel: PlayerViewModel,
    navController: NavHostController,
    onDismiss: () -> Unit
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

    currentSong?.let { song ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            dynamicColor ?: MaterialTheme.colorScheme.primary, // Use dynamic color
                            MaterialTheme.colorScheme.background // Fade to black
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
                            modifier = Modifier.size(12.dp)
                        )
                    },

                    // The track composable (thinner)
                    track = { sliderState ->
                        SliderDefaults.Track(
                            sliderState = sliderState,
                            modifier = Modifier.height(4.dp)
                        )
                    },

                    colors = SliderDefaults.colors(
                        // The thumb (the head) will now use the dynamic theme color.
                        thumbColor = MaterialTheme.colorScheme.primary,

                        // The track that the thumb has passed over will be white.
                        activeTrackColor = Color.White,

                        // The rest of the track will be semi-transparent white.
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = TimeUtils.formatDuration(totalDuration),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                Column {
                    MenuHeader(song = currentSong!!)
                    HorizontalDivider(
                        Modifier,
                        DividerDefaults.Thickness,
                        DividerDefaults.color
                    ) // A nice visual separator

                    MenuOptionItem(
                        text = "Go to artist",
                        icon = Icons.Default.Person,
                        onClick = {
                            onDismiss()
                            // Navigate to the first artist of the song
                            song.artists.firstOrNull()?.let { artist ->
                                navController.navigate(Screen.Artist.createRoute(artist.id))
                                showOptionsMenu = false
                            }
                        }
                    )
                    MenuOptionItem(
                        text = "Go to album",
                        icon = Icons.Default.Album,
                        onClick = {
                            onDismiss()
                            // Navigate to the song's album
                            song.album?.let { album ->
                                navController.navigate(Screen.PlaylistDetail.createRoute(album.id))
                                showOptionsMenu = false
                            }
                        }
                    )
                    MenuOptionItem(
                        text = "Add to playlist",
                        icon = Icons.Default.PlaylistAddCircle,
                        onClick = { /* TODO: Implement "Add to playlist" screen */ }
                    )
                    MenuOptionItem(
                        text = "Start radio",
                        icon = Icons.Default.Radio,
                        onClick = { /* TODO: Implement "Start radio" logic */ }
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}


@Composable
fun MenuOptionItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = text)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}


@Composable
fun MenuHeader(song: Song) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.getBestImageUrl(),
            contentDescription = "Song thumbnail",
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val subtitle = buildString {
                append(song.getArtistNames())
                song.album?.name?.let { append(" • $it") }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}