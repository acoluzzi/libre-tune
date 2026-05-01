package com.colux.libretune.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.colux.libretune.ui.add_to_playlist.AddToPlaylistScreen
import com.colux.libretune.ui.artist.ArtistScreen
import com.colux.libretune.ui.auth.AuthScreen
import com.colux.libretune.ui.create_playlist.CreatePlaylistScreen
import com.colux.libretune.ui.discography.DiscographyScreen
import com.colux.libretune.ui.history.HistoryScreen
import com.colux.libretune.ui.home.HomeScreen
import com.colux.libretune.ui.library.LibraryScreen
import com.colux.libretune.ui.mood_genre.MoodGenreScreen
import com.colux.libretune.ui.player.PlayerViewModel
import com.colux.libretune.ui.playlist.PlaylistDetailScreen
import com.colux.libretune.ui.search.SearchScreen

@Composable
fun Navigation(
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    onShowPlayerFullScreen: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
    ) {

        navigation(startDestination = "home_screen", route = Screen.Home.route) {
            composable("home_screen") {
                HomeScreen(playerViewModel = playerViewModel, navController = navController)
            }

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
                arguments = listOf(navArgument("artistId") { type = NavType.StringType })
            ) {
                val artistId = it.arguments?.getString("artistId")
                if (artistId != null) {
                    DiscographyScreen(
                        artistId = artistId,
                        navController = navController
                    )
                }
            }

            composable(
                route = Screen.AddToPlaylist.route,
                arguments = listOf(navArgument("songId") { type = NavType.StringType })
            ) {
                val songId = it.arguments?.getString("songId")
                if (songId != null) {
                    AddToPlaylistScreen(
                        songId = songId,
                        playerViewModel = playerViewModel,
                        navController = navController
                    )
                }
            }


            composable(Screen.History.route) {
                HistoryScreen(navController = navController, playerViewModel = playerViewModel)
            }

            composable(Screen.Auth.route) {
                AuthScreen(navController = navController)
            }

        }


        navigation(startDestination = "search_screen", route = Screen.Search.route) {
            composable("search_screen") {
                SearchScreen(
                    playerViewModel,
                    navController,
                    onShowPlayerFullScreen
                )
            }

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
                arguments = listOf(navArgument("artistId") { type = NavType.StringType })
            ) {
                val artistId = it.arguments?.getString("artistId")
                if (artistId != null) {
                    DiscographyScreen(
                        artistId = artistId,
                        navController = navController
                    )
                }
            }

            composable(
                route = Screen.AddToPlaylist.route,
                arguments = listOf(navArgument("songId") { type = NavType.StringType })
            ) {
                val songId = it.arguments?.getString("songId")
                if (songId != null) {
                    AddToPlaylistScreen(
                        songId = songId,
                        playerViewModel = playerViewModel,
                        navController = navController
                    )
                }
            }
            composable(Screen.History.route) {
                HistoryScreen(navController = navController, playerViewModel = playerViewModel)
            }

            composable(Screen.MoodGenre.route, arguments = listOf(navArgument("moodGenreId") {
                type =
                    NavType.StringType
            })) { backStackEntry ->
                val moodGenreId = backStackEntry.arguments?.getString("moodGenreId")
                if (moodGenreId != null) {
                    MoodGenreScreen(
                        moodGenreId = moodGenreId,
                        navController = navController,
                        playerViewModel = playerViewModel
                    )
                }
            }
        }

        navigation(startDestination = "library_screen", route = Screen.Library.route) {
            composable("library_screen") { LibraryScreen(navController) }


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
                arguments = listOf(navArgument("artistId") { type = NavType.StringType })
            ) {
                val artistId = it.arguments?.getString("artistId")
                if (artistId != null) {
                    DiscographyScreen(
                        artistId = artistId,
                        navController = navController
                    )
                }
            }

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
                route = Screen.AddToPlaylist.route,
                arguments = listOf(navArgument("songId") { type = NavType.StringType })
            ) {
                val songId = it.arguments?.getString("songId")
                if (songId != null) {
                    AddToPlaylistScreen(
                        songId = songId,
                        playerViewModel = playerViewModel,
                        navController = navController
                    )
                }
            }

            composable(Screen.CreatePlaylist.route) {
                CreatePlaylistScreen(navController = navController)
            }

            composable(Screen.History.route) {
                HistoryScreen(navController = navController, playerViewModel = playerViewModel)
            }
        }


    }
}