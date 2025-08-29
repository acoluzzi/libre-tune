package com.colux.libretune.ui.discography

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscographyScreen(
    artistId: String,
    navController: NavHostController,
    viewModel: DiscographyViewModel = hiltViewModel()
) {
//    val artistDetails by viewModel.artist.collectAsState()
//    var selectedFilter by remember { mutableStateOf("Albums") }
//
//    Column(modifier = Modifier.fillMaxSize()) {
//        Text(artistDetails?.name ?: "Discography" /*...styling...*/)
//
//        // --- Filter Chips ---
//        Row(modifier = Modifier.padding(horizontal = 16.dp)) {
//            FilterChip(
//                selected = selectedFilter == "Albums",
//                onClick = { selectedFilter = "Albums" },
//                label = { Text("Albums") }
//            )
//            Spacer(modifier = Modifier.width(8.dp))
//            FilterChip(
//                selected = selectedFilter == "Singles & EPs",
//                onClick = { selectedFilter = "Singles & EPs" },
//                label = { Text("Singles & EPs") }
//            )
//        }
//
//        // --- Content List ---
//        val contentToShow = if (selectedFilter == "Albums") {
//            artistDetails?.albums
//        } else {
//            artistDetails?.singlesAndEPs
//        }
//
//        LazyColumn {
//            items(contentToShow ?: emptyList()) { playlist ->
//                // Display each playlist/album item
//            }
//        }
//    }
}