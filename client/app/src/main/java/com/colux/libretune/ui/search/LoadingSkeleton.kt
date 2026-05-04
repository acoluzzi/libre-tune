package com.colux.libretune.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.colux.libretune.ui.util.shimmerBackground

@Composable
fun TitleSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            .height(28.dp)
            .fillMaxWidth(0.3f) // Takes up 30% of the width
            .shimmerBackground(RoundedCornerShape(4.dp))
    )
}

@Composable
fun SongItemSkeleton() {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .shimmerBackground(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth(0.7f)
                    .shimmerBackground(RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth(0.4f)
                    .shimmerBackground(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun SearchResultsSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false // Disable scrolling for a better loading feel
    ) {
        // --- First Skeleton Section ---
        item {
            TitleSkeleton()
        }
        items(5) {
            SongItemSkeleton()
        }

        // --- Second Skeleton Section ---
        item {
            TitleSkeleton()
        }
        items(5) {
            SongItemSkeleton()
        }
    }
}