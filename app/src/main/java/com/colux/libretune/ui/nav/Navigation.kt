package com.colux.libretune.ui.nav

import SearchScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.colux.libretune.data.model.Song
import com.colux.libretune.ui.artist.ArtistScreen
import com.colux.libretune.ui.discography.DiscographyScreen
import com.colux.libretune.ui.home.HomeScreen
import com.colux.libretune.ui.library.LibraryScreen
import com.colux.libretune.ui.player.PlayerViewModel
import com.colux.libretune.ui.playlist.PlaylistDetailScreen

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

        navigation(startDestination = "home_screen", route = Screen.Home.route) {
            composable("home_screen") {
                HomeScreen(onSongClick = onSongClick)
            }
        }


        navigation(startDestination = "search_screen", route = Screen.Search.route) {
            composable("search_screen") { SearchScreen(playerViewModel, navController) }

            composable(Screen.Artist.route, arguments = listOf(navArgument("artistId") {
                type =
                    NavType.StringType
            })) { backStackEntry ->
                val artistId = backStackEntry.arguments?.getString("artistId")
                if (artistId != null) {
                    ArtistScreen(artistId = artistId, playerViewModel, navController)
                }
            }

            composable(
                route = Screen.PlaylistDetail.route,
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
            ) { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getString("playlistId")
                if (playlistId != null) {
                    PlaylistDetailScreen(
                        playlistId = playlistId,
                        playerViewModel = playerViewModel,
                        navController = navController
                    )
                }
            }

            composable(
                route = Screen.Discography.route,
                arguments = listOf(navArgument("discographyId") { type = NavType.StringType })
            ) {
                val discographyId = it.arguments?.getString("discographyId")
                if (discographyId != null) {
                    DiscographyScreen(
                        discographyId = discographyId,
                        navController = navController
                    )
                }
            }
        }

        navigation(startDestination = "library_screen", route = Screen.Library.route) {
            composable("library_screen") { LibraryScreen(playerViewModel) }
        }


    }
}