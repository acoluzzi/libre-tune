package com.colux.libretune.data.local.mapper

import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.model.Artist

//suspend fun ArtistWithSongsAndAlbumsAndArtists.toArtistDetails(albumDao: AlbumDao): ArtistDetails {
//    return ArtistDetails(
//        id = this.artist.artistId,
//        name = this.artist.name,
//        description = this.artist.description,
//        images = this.artist.images.map {
//            it.toDataModel()
//        },
//        topSongs = this.songs.map { songWithArtists ->
//            val songEntity = songWithArtists.song
//
//            // 1. For each song, check if it has an albumId
//            val albumForSong = songEntity.albumId?.let { id ->
//                // 2. If it does, use the albumDao to fetch the full album details
//                albumDao.getAlbumWithContent(id).map {
//                    it?.toDataModel()
//                }
//            }?.firstOrNull()
//
//            // 3. Create the final Song object with the hydrated album info
//            Song(
//                id = songEntity.songId,
//                title = songEntity.title,
//                artists = songWithArtists.artists.map { it.toDataModel() },
//                album = albumForSong, // Assign the fetched album here
//                images = songEntity.images.map { it.toDataModel() }
//            )
//        },
//        topSongPlaylist = null, // TODO
//        albums = this.albums.filter {
//            it.album.type == AlbumType.ALBUM
//        }.map {
//            it.toDataModel()
//        },
//        singlesAndEPs = this.albums.filter {
//            it.album.type == AlbumType.SINGLE_EP
//        }.map {
//            it.toDataModel()
//        },
//        featuring = emptyList(), // TODO
//        playlists = emptyList(), // TODO
//        similarArtists = this.relatedArtists.map {
//            it.toDataModel()
//        }
//    )
//}

fun ArtistEntity.toDataModel(): Artist {
    return Artist(
        id = this.artistId,
        name = this.name,
        images = this.images.map { it.toDataModel() }
    )
}

fun Artist.toEntity(): ArtistEntity {
    return ArtistEntity(
        artistId = this.id,
        name = this.name,
        images = this.images.map { it.toEntity() },
        description = null,
        updateTimestamp = System.currentTimeMillis()
    )
}

/**
 * Converts the clean UI model (from the remote source) into the
 * complex database model that the DAO needs for insertion.
 */
//fun ArtistDetails.toArtistWithSongsAndAlbums(): ArtistWithSongsAndAlbumsAndArtists {
//    val albums = this.albums.map {
//        it.toEntity(AlbumType.ALBUM)
//    }
//    val singlesAndEPs = this.singlesAndEPs.map {
//        it.toEntity(AlbumType.SINGLE_EP)
//    }
//    return ArtistWithSongsAndAlbumsAndArtists(
//        artist = ArtistEntity(
//            artistId = this.id,
//            name = this.name,
//            description = this.description,
//            images = this.images.map { it.toEntity() },
//            updateTimestamp = System.currentTimeMillis()
//        ),
//        songs = this.topSongs.map { it.toEntity() },
//        albums = (albums + singlesAndEPs),
//        relatedArtists = this.similarArtists.map { it.toEntity() }
//    )
//}
