package com.colux.libretune.ui.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.colux.libretune.data.model.Song
import com.colux.libretune.ui.components.song.SongItem
import com.colux.libretune.ui.components.song.SongMenu
import com.colux.libretune.ui.player.PlayerViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    // Collect the Flow of PagingData as LazyPagingItems
    val historyItems: LazyPagingItems<HistoryListItem> =
        viewModel.historyItems.collectAsLazyPagingItems()

    val sheetState = rememberModalBottomSheetState()
    var selectedSongForMenu by remember { mutableStateOf<Song?>(null) }

    val songItems = historyItems.itemSnapshotList.items.filterIsInstance<HistoryListItem.SongItem>()
        .mapNotNull { it.songPlayed.song }

    Scaffold(
        topBar = {
            HistoryHeader(
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    count = historyItems.itemCount,
                    key = { index ->
                        // Provide a stable key for each item
                        val item = historyItems.peek(index)
                        when (item) {
                            is HistoryListItem.SongItem -> "song-${item.songPlayed.id}"
                            is HistoryListItem.DateHeader -> "header-${item.date}"
                            null -> "placeholder-$index"
                        }
                    }
                ) { index ->
                    val item = historyItems[index]
                    when (item) {
                        is HistoryListItem.SongItem -> item.songPlayed.song?.let {
                            SongItem(
                                song = it,
                                onClick = {
                                    playerViewModel.playSongList(
                                        songItems,
                                        songItems.indexOf(it)
                                    )
                                },
                                playerViewModel = playerViewModel,
                                navController = navController,
                                onMoreClick = {
                                    selectedSongForMenu = it
                                }
                            )
                        }

                        is HistoryListItem.DateHeader -> DayHeader(date = item.date)
                        null -> { /* Placeholder UI while loading */
                        }
                    }
                }

                // Handle loading states for pagination
                when (historyItems.loadState.append) {
                    is LoadState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }

                    is LoadState.Error -> {
                        item { Text("Error loading more items") }
                    }

                    else -> {}
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }




    if (selectedSongForMenu != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedSongForMenu = null },
            sheetState = sheetState
        ) {
            // We can reuse the menu from the full-screen player
            SongMenu(
                song = selectedSongForMenu!!,
                onClose = {
                    selectedSongForMenu = null
                },
                navController = navController,
                playerViewModel = playerViewModel,
            )
        }
    }
}

/**
 * A custom header for the AddToPlaylistScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryHeader(
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // The first line is the standard top app bar
        TopAppBar(
            title = {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
    }
}

@Composable
fun DayHeader(date: LocalDate) {
    val headerText = when (val daysAgo = ChronoUnit.DAYS.between(date, LocalDate.now())) {
        0L -> "Today"
        1L -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
    }

    Text(
        text = headerText,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}


