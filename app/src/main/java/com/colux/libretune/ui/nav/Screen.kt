package com.colux.libretune.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String, val label: String,
    val outlinedIcon: ImageVector,
    val filledIcon: ImageVector
) {
    data object Home : Screen("home", "Home", Icons.Outlined.Home, Icons.Filled.Home)
    data object Search : Screen("search", "Search", Icons.Outlined.Search, Icons.Filled.Search)
    data object Library : Screen(
        "library", "Library", Icons.Outlined.VideoLibrary,
        Icons.Filled.VideoLibrary
    )

    data object Artist :
        Screen("artist/{artistId}", "Artist", Icons.Default.Person, Icons.Default.Person) {
        fun createRoute(artistId: String) = "artist/$artistId"
    }

    data object PlaylistDetail :
        Screen(
            "playlist_detail/{playlistId}",
            "Playlist Detail",
            Icons.Default.Album,
            Icons.Default.Album
        ) {
        fun createRoute(playlistId: String) = "playlist_detail/$playlistId"
    }

    data object Discography :
        Screen(
            "discography/{artistId}",
            "Discography",
            Icons.Default.Album,
            Icons.Default.Album
        ) {
        fun createRoute(artistId: String) = "discography/$artistId"
    }

    data object AddToPlaylist :
        Screen(
            "add_to_playlist/{songId}",
            "Add to Playlist",
            Icons.Default.PlaylistAdd,
            Icons.Default.PlaylistAdd
        ) {
        fun createRoute(songId: String) = "add_to_playlist/$songId"
    }

    data object CreatePlaylist :
        Screen("create_playlist", "Create Playlist", Icons.Default.Add, Icons.Default.Add) {
        fun createRoute() = "create_playlist"
    }

    data object History :
        Screen("history", "History", Icons.Default.History, Icons.Default.History) {
        fun createRoute() = "history"
    }

}