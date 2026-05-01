package com.colux.libretune.ui.components.song

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.colux.libretune.R
import com.colux.libretune.data.model.Artist
import com.colux.libretune.data.model.Song
import com.colux.libretune.ui.nav.Screen
import com.colux.libretune.ui.player.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongMenu(
    song: Song,
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    onClose: () -> Unit,
    playlistId: String? = null
) {
    val isLiked by playerViewModel.isCurrentSongLiked(song.id)
        .collectAsState(initial = false)

    val isInLocalPlaylist by playerViewModel.isSongInLocalPlaylist(song.id, playlistId)
        .collectAsState(initial = false)

    val isInAlbum = song.album?.id == playlistId


    var showArtistPicker by remember { mutableStateOf(false) }

    Column {
        MenuHeader(song = song)
        HorizontalDivider(
            Modifier,
            DividerDefaults.Thickness,
            DividerDefaults.color
        ) // A nice visual separator

        MenuOptionItem(
            text = "Go to artist" + if (song.artists.size > 1) "s" else "",
            icon = Icons.Default.Person,
            onClick = {
                if (song.artists.size > 1) {
                    // If there are multiple artists, show the picker sheet
                    showArtistPicker = true
                } else {
                    // Otherwise, navigate directly
                    song.artists.firstOrNull()?.let { artist ->
                        navController.navigate(Screen.Artist.createRoute(artist.id))
                        onClose()
                    }
                }
            }
        )

        if (song.album != null && !isInAlbum) {
            MenuOptionItem(
                text = "Go to album",
                icon = Icons.Default.Album,
                onClick = {
                    // Navigate to the first artist of the song
                    song.album?.let { album ->
                        navController.navigate(Screen.PlaylistDetail.createRoute(album.id))
                        onClose()
                    }

                }
            )
        }



        if (!isLiked) {
            MenuOptionItem(
                text = "Add to Favorites",
                icon = Icons.Filled.Favorite,
                onClick = {
                    playerViewModel.onLikeClick(song, isLiked = false)
                    onClose()
                }
            )
        }

        if (isInLocalPlaylist) {
            MenuOptionItem(
                text = "Remove from this playlist",
                icon = Icons.Filled.PlaylistRemove,
                onClick = {
                    playlistId?.let { playerViewModel.removeSongFromPlaylist(song, it) }
                    onClose()
                }
            )

            MenuOptionItem(
                text = "Add to other playlists",
                icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                onClick = {
                    navController.navigate(Screen.AddToPlaylist.createRoute(song.id))
                    onClose()
                }
            )
        } else {
            MenuOptionItem(
                text = "Add to playlist",
                icon = Icons.AutoMirrored.Filled.PlaylistAdd,
                onClick = {
                    navController.navigate(Screen.AddToPlaylist.createRoute(song.id))
                    onClose()
                }
            )
        }


        MenuOptionItem(
            text = "Start radio",
            icon = Icons.Default.Radio,
            onClick = { /* TODO: Implement "Start radio" logic */ }
        )
        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showArtistPicker) {
        ModalBottomSheet(onDismissRequest = { showArtistPicker = false }) {
            Column(modifier = Modifier.navigationBarsPadding()) {
                Text(
                    text = "Artists",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(24.dp)
                )
                LazyColumn {
                    items(song.artists) { artist ->
                        ArtistListItem(
                            artist = artist,
                            onClick = {
                                // Navigate to the selected artist and close both sheets
                                navController.navigate(Screen.Artist.createRoute(artist.id))
                                showArtistPicker = false
                                onClose()
                            }
                        )
                    }
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


@Composable
fun ArtistListItem(
    artist: Artist,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = artist.bestImageForCarousel(),
            contentDescription = artist.name,
            placeholder = painterResource(id = R.drawable.ic_default_artist_avatar_foreground),
            // Shown if the model is null or loading fails
            error = painterResource(id = R.drawable.ic_default_artist_avatar_foreground),
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = artist.name, style = MaterialTheme.typography.bodyLarge)
    }
}