package com.colux.libretune.ui.nav

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.colux.libretune.ui.player.PlayerBar
import com.colux.libretune.ui.player.PlayerFullScreen
import com.colux.libretune.ui.player.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(playerViewModel: PlayerViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    // State for the Modal Bottom Sheet (the full-screen player)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    // Collect state from the PlayerViewModel
    val currentSong by playerViewModel.currentSong.collectAsState()


    // Main layout component
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // This tells the Scaffold's content area to have padding
        // that respects the status bar and navigation bar.
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            val screens = listOf(Screen.Home, Screen.Search, Screen.Library)
            NavigationBar(
                containerColor = Color.Transparent, // Make the bar's background transparent
                tonalElevation = 0.dp // Remove any default shadow
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    val selected =
                        currentDestination?.hierarchy?.any { it.route == screen.route } == true

                    NavigationBarItem(
                        selected = selected,
                        label = { Text(screen.label) },
                        icon = {   // Switch between filled and outlined icons
                            val icon = if (selected) screen.filledIcon else screen.outlinedIcon
                            Icon(icon, contentDescription = screen.label)
                        },
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
                        }, colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.5f),
                            unselectedTextColor = Color.White.copy(alpha = 0.5f),
                            indicatorColor = Color.Transparent // Hide the selection indicator
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            Navigation(
                navController = navController,
                playerViewModel,
                onSongClick = { playlist, songIndex ->
                    // When a song is clicked, update the playerViewModel with the new song
                    playerViewModel.playPlaylist(playlist, songIndex)
                }
            )

            if (currentSong != null) {
                PlayerBar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .clickable { showBottomSheet = true },
                    playerViewModel = playerViewModel
                )
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            dragHandle = null,
            modifier = Modifier.fillMaxSize()
        ) {
            currentSong?.let {
                PlayerFullScreen(playerViewModel)
            }
        }
    }
}