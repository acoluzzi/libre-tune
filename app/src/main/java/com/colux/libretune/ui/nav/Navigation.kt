package com.colux.libretune.ui.nav

import SearchScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.colux.libretune.data.model.Song
import com.colux.libretune.ui.artist.ArtistScreen
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
        composable(Screen.Search.route) { SearchScreen(playerViewModel, navController) }
        composable(Screen.Library.route) { LibraryScreen(playerViewModel) }

        composable(Screen.Artist.route, arguments = listOf(navArgument("artistId") {
            type =
                NavType.StringType
        })) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getString("artistId")
            if (artistId != null) {
                ArtistScreen(artistId = artistId, playerViewModel)
            }
        }
    }
}