package com.colux.libretune.ui.home


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.colux.libretune.ui.search.SongItemSkeleton
import com.colux.libretune.ui.search.TitleSkeleton
import com.colux.libretune.ui.util.shimmerBackground


/**
 * A placeholder for a single item in a horizontal carousel (e.g., an album).
 */
@Composable
fun CarouselItemSkeleton() {
    Column(
        modifier = Modifier.width(160.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Square shape for album art
                .shimmerBackground(RoundedCornerShape(8.dp))
        )
        Box(
            modifier = Modifier
                .height(20.dp)
                .fillMaxWidth(0.8f)
                .shimmerBackground(RoundedCornerShape(4.dp))
        )
    }
}

/**
 * A placeholder for a full horizontal carousel (e.g., "Albums").
 */
@Composable
fun ArtistCarouselSkeleton() {
    Column {
        TitleSkeleton(modifier = Modifier.padding(bottom = 8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(5) { // Show 5 placeholder carousel items
                CarouselItemSkeleton()
            }
        }
    }
}

@Composable
fun HomeScreenSkeleton(innerPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        userScrollEnabled = false // Disable scrolling for a better loading feel
    ) {
        item {
            TitleSkeleton()
        }

        items(3) {
            SongItemSkeleton()
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            ArtistCarouselSkeleton()
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            ArtistCarouselSkeleton()
        }

        item {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}
