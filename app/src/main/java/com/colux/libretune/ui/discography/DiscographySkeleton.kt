package com.colux.libretune.ui.discography

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.colux.libretune.ui.search.TitleSkeleton
import com.colux.libretune.ui.util.shimmerBackground

@Composable
fun FilterChipSkeleton() {
    Box(
        modifier = Modifier
            .size(width = 100.dp, height = 32.dp)
            .shimmerBackground(RoundedCornerShape(8.dp))
    )
}

@Composable
fun AlbumListItemSkeleton() {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
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
fun DiscographyScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Header Skeleton
        TitleSkeleton(modifier = Modifier.padding(top = 16.dp))

        // Filter Chips Skeleton
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipSkeleton()
            FilterChipSkeleton()
        }

        // List Skeleton
        Column(modifier = Modifier.fillMaxWidth()) {
            repeat(10) {
                AlbumListItemSkeleton()
            }
        }
    }
}