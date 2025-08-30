package com.colux.libretune.ui.discography

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.colux.libretune.ui.components.album.ArtistAlbum
import com.colux.libretune.ui.nav.Screen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscographyScreen(
    discographyId: String,
    navController: NavHostController,
    viewModel: DiscographyViewModel = hiltViewModel()
) {
    val albums by viewModel.albums.collectAsState()
    val singlesEp by viewModel.singlesEp.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedFilter by remember { mutableStateOf("Albums") }


    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (albums.isNotEmpty() || singlesEp.isNotEmpty()) {

        Column(modifier = Modifier.fillMaxSize()) {
            Text("Discography", style = MaterialTheme.typography.headlineLarge)

            // --- Filter Chips ---
            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                FilterChip(
                    selected = selectedFilter == "Albums",
                    onClick = { selectedFilter = "Albums" },
                    label = { Text("Albums") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = selectedFilter == "Singles & EPs",
                    onClick = { selectedFilter = "Singles & EPs" },
                    label = { Text("Singles & EPs") }
                )
            }

            // --- Content List ---
            val contentToShow = if (selectedFilter == "Albums") {
                albums
            } else {
                singlesEp
            }

            LazyColumn {
                items(contentToShow) { playlist ->
                    ArtistAlbum(
                        playlist = playlist,
                        onClick = {
                            navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id))
                        }
                    )
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No discography available", style = MaterialTheme.typography.bodyLarge)
        }

    }

}