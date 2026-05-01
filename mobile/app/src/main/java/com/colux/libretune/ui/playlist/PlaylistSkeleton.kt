package com.colux.libretune.ui.playlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.colux.libretune.ui.search.SongItemSkeleton
import com.colux.libretune.ui.util.shimmerBackground

@Composable
fun PlaylistDetailSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        BannerSkeleton()

        Spacer(modifier = Modifier.height(16.dp))
        // A smaller skeleton for the subtitle
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(20.dp)
                .fillMaxWidth(0.5f)
                .shimmerBackground(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(16.dp))

        SongsSkeleton(10)
    }
}

@Composable
fun SongsSkeleton(count: Int = 10) {
    Column(modifier = Modifier.fillMaxWidth()) {
        repeat(count) {
            SongItemSkeleton()
        }
    }
}

/**
 * A reusable placeholder for banner images.
 * (This is the same as the one from ArtistScreen).
 */
@Composable
fun BannerSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp) // Match the playlist screen's image height
            .shimmerBackground()
    )
}