package com.colux.libretune.ui.history

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun HistoryScreen(navController: NavController) {
    val viewModel: HistoryViewModel = hiltViewModel()
    //val history by viewModel.playbackHistory.collectAsState()

//    Scaffold(topBar = { /* ... with back button ... */ }) { //padding ->
////        LazyColumn(modifier = Modifier.padding(padding)) {
////            items(history) { songWithDetails ->
////                // Use your SongItem to display the song
////            }
////        }
//    }
}