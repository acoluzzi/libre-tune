package com.colux.libretune.ui.nav

import SearchScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.colux.libretune.data.model.Song
import com.colux.libretune.ui.home.HomeScreen
import com.colux.libretune.ui.library.LibraryScreen
import com.colux.libretune.ui.player.PlayerViewModel

@Composable
fun Navigation(
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    onSongClick: (playlist: List<Song>, songIndex: Int) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
    ) {
        composable(Screen.Home.route) {
            HomeScreen(onSongClick = onSongClick)
        }
        composable(Screen.Search.route) { SearchScreen(playerViewModel) }
        composable(Screen.Library.route) { LibraryScreen(playerViewModel) }
    }
}