package com.colux.libretune.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.colux.libretune.data.model.MoodGenreItem
import kotlin.random.Random

// Helper function to generate a random color
fun generateRandomColor(): Color {
    val red = Random.nextInt(256)
    val green = Random.nextInt(256)
    val blue = Random.nextInt(256)

    return Color(red, green, blue)
}

@Composable
fun MoodGenreCard(
    mood: MoodGenreItem,
    modifier: Modifier = Modifier,
    onClickListener: (MoodGenreItem) -> Unit
) {
    val shadeColor = remember { generateRandomColor() }

    Card(modifier = modifier.height(100.dp), onClick = { onClickListener(mood) }) {
        Box(
            modifier = Modifier.fillMaxSize(), // The outer Box takes all space
            contentAlignment = Alignment.CenterStart // Align content to the center-start
        ) {
            // Colored strip on the left edge
            Box(
                modifier = Modifier
                    .fillMaxHeight() // Fills the height of the card
                    .width(8.dp)     // Fixed width for the shade
                    .background(shadeColor)
            )

            // Content (Text)
            Text(
                text = mood.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface, // Text color for contrast
                modifier = Modifier.padding(start = 24.dp, end = 16.dp) // Padded to clear the shade
            )
        }
    }
}
