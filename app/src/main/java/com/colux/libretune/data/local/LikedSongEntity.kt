package com.colux.libretune.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.colux.libretune.data.model.Song

@Entity(tableName = "liked_songs")
data class LikedSongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val imageUrl: String
)

// Helper function to convert our entity to the domain model we use in the UI
fun LikedSongEntity.toSong(): Song {
    return Song(
        id = this.id,
        title = this.title,
        artist = this.artist,
        imageUrl = this.imageUrl,
        mediaUrl = null // mediaUrl is fetched on demand, not stored
    )
}