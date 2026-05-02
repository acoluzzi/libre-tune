package com.colux.libretune.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.colux.libretune.shared.currentPlatform

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "LibreTune Desktop",
        state = rememberWindowState(),
    ) {
        App()
    }
}

@Composable
private fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "LibreTune Desktop",
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Text(
                        text = "Running on ${currentPlatform().name}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}
