package com.colux.libretune.data.repository

import androidx.room.withTransaction
import com.colux.libretune.data.local.AppDatabase
import com.colux.libretune.data.local.entity.PlaylistEntity
import com.colux.libretune.data.local.join.PlaylistArtistCrossRef
import com.colux.libretune.data.local.mapper.toDataModel
import com.colux.libretune.data.local.mapper.toEntity
import com.colux.libretune.data.model.Playlist
import com.colux.libretune.data.model.wrapper.AlbumWithArtists
import com.colux.libretune.data.remote.tube.YouTubeExtractionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.logging.Logger

class AlbumRepository(
    private val remote: YouTubeExtractionRepository,
    private val db: AppDatabase,
) {
    private val logger = Logger.getLogger("AlbumRepository")

    fun getArtistDiscography(artistId: String): Flow<List<Playlist>> {
        logger.info { "Starting getArtistDiscography for $artistId" }

        val albumsFlow: Flow<List<PlaylistEntity>> = db.playlistDao().getAlbumsByArtistId(artistId)
        val singlesFlow: Flow<List<PlaylistEntity>> =
            db.playlistDao().getSinglesByArtistId(artistId)

        val albumsAndSinglesFlow = combine(albumsFlow, singlesFlow) { albums, singles ->
            albums + singles
        }


        val albumsWithArtistsFlow: Flow<List<AlbumWithArtists>> =
            albumsAndSinglesFlow.map { albums ->
                albums.map { album ->
                    val albumArtists =
                        db.artistDao().getArtistsByAlbumId(album.playlistId).firstOrNull()
                    AlbumWithArtists(
                        album,
                        albumArtists ?: emptyList()
                    )
                }
            }

        return albumsWithArtistsFlow.map {
            it.sortedByDescending { albumWithArtists ->
                albumWithArtists.albumEntity.releaseYear
            }.map { albumWithArtists ->
                albumWithArtists.toDataModel()
            }
        }
    }

    suspend fun refreshArtistDiscography(artistId: String) {
        val artist = db.artistDao().getArtistById(artistId)

        if (artist == null || artist.discographyId?.isEmpty() == true) {
            return
        }

        val albumDiscography = remote.discography(artist.discographyId!!, artist.albumsParams)
        val singlesEpDiscography = remote.discography(artist.discographyId, artist.singlesEpsParams)

        val albumEntities = (albumDiscography + singlesEpDiscography).map {
            it.toEntity()
        }

        val artistAlbumLinks = albumEntities.map { album ->
            PlaylistArtistCrossRef(
                artistId = artist.artistId,
                playlistId = album.playlistId
            )
        }

        db.withTransaction {
            db.playlistDao().upsertAll(albumEntities)

            db.playlistDao().linkAlbumToArtists(artistAlbumLinks)
        }

    }
}