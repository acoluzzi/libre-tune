package com.colux.libretune.ui.components.song


import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NowPlayingIndicator(isPlaying: Boolean) {

    Column(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.4f))
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isPlaying) {
                AnimatingBars()
            } else {
                PausedBars()
            }


        }
    }


}


@Composable
private fun AnimatingBars() {
    val infiniteTransition = rememberInfiniteTransition(label = "NowPlayingTransition")
    val animatedValues = (1..3).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 300 + (index * 200),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "BarAnimation$index"
        )
    }
    animatedValues.forEach { animatedValue ->
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight(fraction = animatedValue.value)
                .background(MaterialTheme.colorScheme.onSurface)
        )
    }
}

@Composable
private fun PausedBars() {
    // These are just 3 static bars with fixed heights
    Box(
        modifier = Modifier
            .width(3.dp)
            .fillMaxHeight(0.4f)
            .background(MaterialTheme.colorScheme.onSurface)
    )
    Box(
        modifier = Modifier
            .width(3.dp)
            .fillMaxHeight(0.6f)
            .background(MaterialTheme.colorScheme.onSurface)
    )
    Box(
        modifier = Modifier
            .width(3.dp)
            .fillMaxHeight(0.3f)
            .background(MaterialTheme.colorScheme.onSurface)
    )
}