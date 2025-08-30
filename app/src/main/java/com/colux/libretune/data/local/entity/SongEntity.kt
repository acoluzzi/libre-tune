package com.colux.libretune.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.colux.libretune.data.model.Song

@Entity(
    tableName = "songs",
    // An index on albumId will speed up queries for an album's songs
    indices = [Index("albumId")],
    foreignKeys = [
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["albumId"],
            childColumns = ["albumId"],
            onDelete = ForeignKey.CASCADE // If an album is deleted, its songs are deleted too
        )
    ]
)
data class SongEntity(
    @PrimaryKey val songId: String,
    val title: String,
    val albumId: String?,
    val images: List<ImageAttribute>
) {
    companion object {
        fun from(song: Song): SongEntity {
            return SongEntity(
                songId = song.id,
                title = song.title,
                albumId = song.artists.firstOrNull()?.id,
                images = song.images.map {
                    ImageAttribute(
                        url = it.url,
                        width = it.width,
                        height = it.height
                    )
                }
            )
        }
    }


//    fun toSong(): Song {
//        return Song(
//            id = this.id,
//            title = this.title,
//            artists = this.artists.map {
//                Artist(
//                    id = it.id,
//                    name = it.name,
//                    images = it.images.map { image ->
//                        Image(
//                            url = image.url,
//                            width = image.width,
//                            height = image.height
//                        )
//                    }
//                )
//            },
//            images = this.images.map {
//                Image(
//                    url = it.url,
//                    width = it.width,
//                    height = it.height
//                )
//            },
//        )
//    }
}
