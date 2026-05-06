package com.colux.libretune.desktop

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.colux.libretune.desktop.ui.AuthScreen
import com.colux.libretune.desktop.ui.HomeScreen
import com.colux.libretune.desktop.ui.LibraryScreen
import com.colux.libretune.desktop.ui.SearchScreen
import com.colux.libretune.desktop.viewmodel.AuthViewModel
import com.colux.libretune.desktop.viewmodel.DesktopPlayerViewModel
import com.colux.libretune.desktop.viewmodel.HomeViewModel
import com.colux.libretune.desktop.viewmodel.LibraryViewModel
import com.colux.libretune.desktop.viewmodel.PlayerState
import com.colux.libretune.desktop.viewmodel.SearchViewModel

fun main() = application {
    AppContainer.initialise()

    Window(
        onCloseRequest = ::exitApplication,
        title = "LibreTune",
        state = rememberWindowState(),
    ) {
        LibreTuneApp()
    }
}

private enum class NavDestination(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    SEARCH("Search", Icons.Default.Search),
    LIBRARY("Library", Icons.Default.LibraryMusic),
    ACCOUNT("Account", Icons.Default.AccountCircle),
}

private val appViewModelStoreOwner = object : ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
}

@Composable
private fun LibreTuneApp() {
    CompositionLocalProvider(LocalViewModelStoreOwner provides appViewModelStoreOwner) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
            val destinations = NavDestination.entries

            val playerVm: DesktopPlayerViewModel = viewModel { DesktopPlayerViewModel(AppContainer.songRepository) }
            val playerState by playerVm.state.collectAsState()

            Scaffold(
                bottomBar = { PlayerBar(playerState, playerVm::togglePause, playerVm::stop) },
            ) { contentPadding ->
                Row(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
                    NavigationRail {
                        destinations.forEachIndexed { index, dest ->
                            NavigationRailItem(
                                selected = selectedIndex == index,
                                onClick = { selectedIndex = index },
                                icon = { Icon(dest.icon, contentDescription = dest.label) },
                                label = { Text(dest.label) },
                            )
                        }
                    }

                    when (destinations[selectedIndex]) {
                        NavDestination.HOME -> {
                            val vm: HomeViewModel = viewModel { HomeViewModel(AppContainer.homeRepository) }
                            HomeScreen(vm, onSongClick = playerVm::playSong)
                        }
                        NavDestination.SEARCH -> {
                            val vm: SearchViewModel = viewModel { SearchViewModel(AppContainer.searchRepository) }
                            SearchScreen(vm, onSongClick = playerVm::playSong)
                        }
                        NavDestination.LIBRARY -> {
                            val vm: LibraryViewModel = viewModel { LibraryViewModel(AppContainer.playlistRepository, AppContainer.artistRepository) }
                            LibraryScreen(vm)
                        }
                        NavDestination.ACCOUNT -> {
                            val vm: AuthViewModel = viewModel { AuthViewModel(AppContainer.backendSyncRepository, AppContainer.librarySyncOrchestrator) }
                            AuthScreen(vm)
                        }
                    }
                }
            }
        }
    }
    } // CompositionLocalProvider
}

@Composable
private fun PlayerBar(
    state: PlayerState,
    onTogglePause: () -> Unit,
    onStop: () -> Unit,
) {
    if (state is PlayerState.Idle) return

    BottomAppBar {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (state) {
                is PlayerState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.song.title,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = state.song.getArtistNames(),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                is PlayerState.Playing -> {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.song.title,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = state.song.getArtistNames(),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    IconButton(onClick = onTogglePause) {
                        Icon(
                            imageVector = if (state.paused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (state.paused) "Resume" else "Pause",
                        )
                    }
                    IconButton(onClick = onStop) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop")
                    }
                }
                is PlayerState.Error -> {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.song.title,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "Error: ${state.message}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    IconButton(onClick = onStop) {
                        Icon(Icons.Default.Stop, contentDescription = "Dismiss")
                    }
                }
                else -> {}
            }
        }
    }
}

