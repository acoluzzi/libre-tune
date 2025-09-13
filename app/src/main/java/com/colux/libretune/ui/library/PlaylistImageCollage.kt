package com.colux.libretune.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun PlaylistImageCollage(
    imageUrls: List<String>,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val displayedImageUrls = imageUrls.take(4) // Take up to 4 images for the collage

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant) // Placeholder background
    ) {
        if (displayedImageUrls.isEmpty()) {
            // Show a default icon or colored box if no images are available
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            )
        } else if (displayedImageUrls.size == 1) {
            // Single image fills the whole box
            AsyncImage(
                model = displayedImageUrls.first(),
                contentDescription = null, // Content description depends on context
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Grid for 2, 3, or 4 images
            Column(modifier = Modifier.fillMaxSize()) {
                // First row (up to 2 images)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AsyncImage(
                        model = displayedImageUrls.getOrNull(0),
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentScale = ContentScale.Crop
                    )
                    if (displayedImageUrls.size >= 2) {
                        AsyncImage(
                            model = displayedImageUrls.getOrNull(1),
                            contentDescription = null,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (displayedImageUrls.size == 1) {
                        // If only one image, fill the second half with a blank space or placeholder
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color.Transparent)
                        )
                    }
                }
                // Second row (up to 2 images)
                if (displayedImageUrls.size >= 3) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        AsyncImage(
                            model = displayedImageUrls.getOrNull(2),
                            contentDescription = null,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentScale = ContentScale.Crop
                        )
                        if (displayedImageUrls.size >= 4) {
                            AsyncImage(
                                model = displayedImageUrls.getOrNull(3),
                                contentDescription = null,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (displayedImageUrls.size == 3) {
                            // If only three images, fill the second half with a blank space or placeholder
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(Color.Transparent)
                            )
                        }
                    }
                }
            }
        }
    }
}