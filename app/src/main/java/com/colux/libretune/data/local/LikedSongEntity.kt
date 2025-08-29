package com.colux.libretune.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.colux.libretune.data.model.Artist
import com.colux.libretune.data.model.Image
import com.colux.libretune.data.model.Song
import kotlinx.serialization.Serializable

@Serializable
data class LikedSongArtist(
    val name: String,
    val id: String,
    val imageUrl: String? = null
)

@Serializable
data class LikedSongImage(
    val url: String,
    val width: Int? = null,
    val height: Int? = null
)

@Entity(tableName = "liked_songs")
data class LikedSongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artists: List<LikedSongArtist>,
    val images: List<LikedSongImage>
) {
    companion object {
        fun from(song: Song): LikedSongEntity {
            return LikedSongEntity(
                id = song.id,
                title = song.title,
                artists = song.artists.map {
                    LikedSongArtist(
                        id = it.id,
                        name = it.name,
                        imageUrl = it.imageUrl
                    )
                },
                images = song.images.map {
                    LikedSongImage(
                        url = it.url,
                        width = it.width,
                        height = it.height
                    )
                }
            )
        }
    }
}

// Helper function to convert our entity to the domain model we use in the UI
fun LikedSongEntity.toSong(): Song {
    return Song(
        id = this.id,
        title = this.title,
        artists = this.artists.map {
            Artist(
                id = it.id,
                name = it.name,
                imageUrl = it.imageUrl
            )
        },
        images = this.images.map {
            Image(
                url = it.url,
                width = it.width,
                height = it.height
            )
        },
    )
}