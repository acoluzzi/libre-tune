package com.colux.libretune.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.colux.libretune.ui.util.shimmerBackground

@Composable
fun MoodGenreSkeleton() {
    Card(modifier = Modifier.height(100.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shimmerBackground(),
            contentAlignment = Alignment.Center
        ) {
        }
    }
}