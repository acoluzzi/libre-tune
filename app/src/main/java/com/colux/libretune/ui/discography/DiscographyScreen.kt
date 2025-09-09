package com.colux.libretune.ui.discography

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.colux.libretune.data.model.Playlist
import com.colux.libretune.data.model.PlaylistType
import com.colux.libretune.ui.nav.Screen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscographyScreen(
    artistId: String,
    navController: NavHostController,
    viewModel: DiscographyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is DiscographyUiState.Loading -> {
            DiscographyScreenSkeleton()
        }

        is DiscographyUiState.Success -> {
            DiscographyContent(
                allReleases = state.albums,
                navController = navController
            )
        }

        is DiscographyUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${state.message}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscographyContent(
    allReleases: List<Playlist>,
    navController: NavHostController
) {
    // 1. State for the currently selected filter chip.
    var selectedFilter by remember { mutableStateOf(PlaylistType.ALBUM) }

    // 2. Filter and sort the list based on the selected chip.
    // This derived state will automatically re-calculate when the filter changes.
    val filteredAndSortedList = remember(selectedFilter, allReleases) {
        allReleases
            .filter { it.type == selectedFilter }
            .sortedByDescending { it.releaseYear }
    }

    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        selectedLabelColor = MaterialTheme.colorScheme.primary,
    )



    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // --- Header (You can add artist name here if needed) ---
        Text(
            text = "Releases",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        // --- 3. Filter Chips ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (allReleases.any { it.type == PlaylistType.ALBUM }) {
                FilterChip(
                    selected = selectedFilter == PlaylistType.ALBUM,
                    onClick = { selectedFilter = PlaylistType.ALBUM },
                    label = { Text("Albums") },
                    colors = chipColors,
                    border = FilterChipDefaults.filterChipBorder(
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        enabled = true,
                        selected = selectedFilter == PlaylistType.ALBUM
                    )
                )
            }

            if (allReleases.any { it.type == PlaylistType.SINGLE }) {
                FilterChip(
                    selected = selectedFilter == PlaylistType.SINGLE,
                    onClick = { selectedFilter = PlaylistType.SINGLE },
                    label = { Text("Singles") },
                    colors = chipColors,
                    border = FilterChipDefaults.filterChipBorder(
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        enabled = true,
                        selected = selectedFilter == PlaylistType.SINGLE
                    )
                )
            }

            if (allReleases.any { it.type == PlaylistType.EP }) {
                FilterChip(
                    selected = selectedFilter == PlaylistType.EP,
                    onClick = { selectedFilter = PlaylistType.EP },
                    label = { Text("EPs") },
                    colors = chipColors,
                    border = FilterChipDefaults.filterChipBorder(
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        enabled = true,
                        selected = selectedFilter == PlaylistType.EP
                    )
                )
            }
        }

        // --- 4. Content List ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp) // Space for mini-player
        ) {
            items(filteredAndSortedList) { playlist ->
                AlbumListItem(
                    playlist = playlist,
                    onClick = {
                        navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id))
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }


    }
}

/**
 * A reusable composable for displaying an album/playlist in a list.
 */
@Composable
fun AlbumListItem(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = playlist.images.firstOrNull()?.url,
            contentDescription = "Album art for ${playlist.name}",
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = playlist.releaseYear?.toString() ?: "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

    }
}