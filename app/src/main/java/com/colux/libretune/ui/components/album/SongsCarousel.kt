package com.colux.libretune.ui.components.album

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.colux.libretune.data.model.Song

@Composable
fun SongsCarousel(
    title: String,
    songs: List<Song>,
    onSongClick: (songIndex: Int) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            itemsIndexed(songs) { index, song ->
                // Pass the 'index' of the song, which is an Int
                SongCard(song = song, onSongClick = { onSongClick(index) })
            }
        }
    }
}