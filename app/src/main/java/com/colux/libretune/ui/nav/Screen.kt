package com.colux.libretune.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Search : Screen("search", "Search", Icons.Default.Search)
    data object Library : Screen("library", "Your Library", Icons.AutoMirrored.Filled.List)
    data object Artist : Screen("artist/{artistId}", "Artist", Icons.Default.Person) {
        fun createRoute(artistId: String) = "artist/$artistId"
    }

    data object PlaylistDetail :
        Screen("playlist_detail/{playlistId}", "Playlist Detail", Icons.Default.Album) {
        fun createRoute(playlistId: String) = "playlist_detail/$playlistId"
    }
}