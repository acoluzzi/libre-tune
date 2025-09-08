package com.colux.libretune.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.colux.libretune.data.model.Song
import com.colux.libretune.ui.components.playlist.PlaylistCarousel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    onSongClick: (playlist: List<Song>, songIndex: Int) -> Unit,
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    scope: CoroutineScope = rememberCoroutineScope()
) {

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                navController = navController,
                closeDrawer = { scope.launch { drawerState.close() } })
        }
    ) {
        val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
        val madeForYou by viewModel.madeForYou.collectAsState()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp) // Space between carousels
        ) {
            item {
                // A nice greeting
                Text(
                    text = "Good afternoon", // You can make this dynamic based on time!
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            item {
                PlaylistCarousel(
                    title = "Recently Played",
                    playlists = listOf(),
                    onItemClick = { index ->
                        onSongClick(recentlyPlayed, index)
                    })
            }

            item {
                PlaylistCarousel(
                    title = "Made for You",
                    playlists = listOf(),
                    onItemClick = { index ->
                        onSongClick(madeForYou, index)
                    })
            }



            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }


}


@Composable
fun AppDrawer(navController: NavController, closeDrawer: () -> Unit) {
    ModalDrawerSheet {
        // ... Drawer header (e.g., app logo)
        NavigationDrawerItem(
            label = { Text("History") },
            icon = { Icon(Icons.Default.History, contentDescription = "History") },
            selected = false,
            onClick = { navController.navigate("history"); closeDrawer() }
        )
        NavigationDrawerItem(
            label = { Text("Statistics") },
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Statistics") },
            selected = false,
            onClick = { navController.navigate("statistics"); closeDrawer() }
        )
    }
}