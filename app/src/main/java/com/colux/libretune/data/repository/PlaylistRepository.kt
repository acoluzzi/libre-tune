package com.colux.libretune.data.repository

import androidx.room.withTransaction
import com.colux.libretune.data.local.AppDatabase
import com.colux.libretune.data.local.entity.AlbumEntity
import com.colux.libretune.data.local.entity.AlbumType
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.PlaylistEntity
import com.colux.libretune.data.local.entity.SongEntity
import com.colux.libretune.data.local.join.AlbumArtistCrossRef
import com.colux.libretune.data.local.join.PlaylistSongCrossRef
import com.colux.libretune.data.local.mapper.toDataModel
import com.colux.libretune.data.local.mapper.toEntity
import com.colux.libretune.data.local.wrapper.SongWithAlbumAndArtists
import com.colux.libretune.data.model.PlaylistDetails
import com.colux.libretune.data.model.PlaylistType
import com.colux.libretune.data.remote.tube.YouTubeExtractionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val remote: YouTubeExtractionRepository,
    private val db: AppDatabase,
) {

    private val logger = Logger.getLogger("PlaylistRepository")

    // Define how long the cache should be valid (e.g., 60 minutes)
    private val cacheTtlMillis = TimeUnit.MINUTES.toMillis(60)


    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPlaylistDetails(id: String): Flow<PlaylistDetails?> {
        logger.info { "Starting getPlaylistDetails for $id" }

        val playlistFlow: Flow<PlaylistEntity?> = db.playlistDao().getPlaylist(id)
        val albumFlow: Flow<AlbumEntity?> = db.albumDao().getAlbum(id)

        logger.info { "building albumArtistsFlow $id" }
        val albumArtistsFlow: Flow<List<ArtistEntity>> = albumFlow.flatMapLatest { album ->
            if (album == null) {
                flowOf(emptyList()) // Emit an empty list if there's no album
            } else {
                db.artistDao().getArtistsByAlbumId(album.albumId)
            }
        }


        logger.info { "building albumSongsFlow $id" }
        val albumSongsFlow: Flow<List<SongEntity>> = albumFlow.map { album ->
            if (album == null) return@map emptyList()
            db.songDao().getSongsByAlbumId(album.albumId)
        }


        logger.info { "building playlistSongsFlow $id" }
        val playlistSongsFlow: Flow<List<SongEntity>> = playlistFlow.map { playlist ->
            if (playlist == null) return@map emptyList()
            db.songDao().getSongsInPlaylist(playlist.playlistId)
        }


        logger.info { "building songsWithAlbumsAndArtistsFlow $id" }
        val songsWithAlbumsAndArtistsFlow: Flow<List<SongWithAlbumAndArtists>> =
            combine(playlistSongsFlow, albumSongsFlow) { albumSongs, playlistSongs ->
                albumSongs + playlistSongs
            }.map { songs ->
                songs.map { song ->
                    val songAlbum = db.albumDao().getAlbumById(song.albumId ?: "")

                    val albumArtists = songAlbum?.albumId?.let {
                        db.artistDao().getArtistsByAlbumId(it)
                            .firstOrNull() ?: emptyList()
                    } ?: emptyList()

                    SongWithAlbumAndArtists(
                        song,
                        songAlbum,
                        albumArtists
                    )
                }
            }


        logger.info { "building combined Flow $id" }
        return combine(
            playlistFlow,
            albumFlow,
            songsWithAlbumsAndArtistsFlow,
            albumArtistsFlow
        ) { playlist, album, songs, artists ->

            logger.info { "Combining flows" }
            if (playlist == null && album == null) return@combine null

            logger.info { "Building final entity" }

            logger.info { "playlist: $playlist" }
            logger.info { "album: $album" }
            logger.info { "songs count: ${songs.size}" }
            logger.info { "artists count: ${artists.size}" }

            PlaylistDetails(
                name = playlist?.name ?: album?.name ?: "Unknown",
                images = ((playlist?.images ?: emptyList()) + (album?.images ?: emptyList())).map {
                    it.toDataModel()
                },
                type = playlist?.let { PlaylistType.PLAYLIST } ?: album?.let {
                    if (it.type == AlbumType.ALBUM) PlaylistType.ALBUM else PlaylistType.SINGLE_EP
                } ?: PlaylistType.PLAYLIST,
                artists = artists.map { it.toDataModel() },
                songs = songs.map { it.toDataModel() },
                relatedPlaylists = emptyList()
            )
        }
    }


    private suspend fun shouldFetch(id: String): Boolean {
        // Fetch the artist record just to check its timestamp
        val cachedPlaylistObj = db.playlistDao().getPlaylistById(id)
        val cachedAlbumObj = db.albumDao().getAlbumById(id)

        logger.info { "cachedPlaylistObj: $cachedPlaylistObj" }
        logger.info { "cachedAlbumObj: $cachedAlbumObj" }

        // Always fetch if there's no data
        if (cachedPlaylistObj == null && cachedAlbumObj == null) return true

        val lastUpdate = cachedPlaylistObj?.updateTimestamp ?: cachedAlbumObj?.updateTimestamp ?: 0
        if (lastUpdate == 0L) return true

        // Fetch if the data is older than our TTL
        val isStale =
            (System.currentTimeMillis() - lastUpdate) > cacheTtlMillis
        return isStale
    }

    suspend fun refreshPlaylistDetails(id: String) {

        logger.info { "Starting refreshPlaylistDetails for $id" }
        if (shouldFetch(id)) {
            logger.info { "Fetching data from remote" }
            updatePlaylistDetailsFromRemote(id)
        }
    }

    private suspend fun updatePlaylistDetailsFromRemote(playlistId: String) {
        try {

            val remoteDetails = remote.getPlaylistDetails(playlistId) ?: return

            logger.info { "Received data from remote: $remoteDetails" }

            val artistsEntities = mutableListOf<ArtistEntity>()
            val albumEntities = mutableListOf<AlbumEntity>()
            val songEntities = mutableListOf<SongEntity>()
            val playlistEntities = mutableListOf<PlaylistEntity>()
            val albumArtistLinks = mutableListOf<AlbumArtistCrossRef>()
            val playlistSongsLinks = mutableListOf<PlaylistSongCrossRef>()


            // --- MAPPING LOGIC ---
            when (remoteDetails.type) {
                PlaylistType.ALBUM, PlaylistType.SINGLE_EP -> {
                    albumEntities.add(
                        AlbumEntity(
                            albumId = playlistId,
                            name = remoteDetails.name,
                            images = remoteDetails.images.map {
                                it.toEntity()
                            },
                            type = if (remoteDetails.type == PlaylistType.ALBUM) AlbumType.ALBUM else AlbumType.SINGLE_EP,
                            updateTimestamp = System.currentTimeMillis()
                        )
                    )
                }

                PlaylistType.PLAYLIST -> {
                    playlistEntities.add(
                        PlaylistEntity(
                            playlistId = playlistId,
                            name = remoteDetails.name,
                            images = remoteDetails.images.map {
                                it.toEntity()
                            },
                            updateTimestamp = System.currentTimeMillis(),
                            isLocal = false
                        )
                    )

                    playlistSongsLinks.addAll(
                        remoteDetails.songs.map { song ->
                            PlaylistSongCrossRef(
                                playlistId = playlistId,
                                songId = song.id
                            )
                        }
                    )
                }
            }

            val songAlbums = remoteDetails.songs.mapNotNull {
                it.album
            }

            artistsEntities.addAll(
                remoteDetails.artists.map {
                    it.toEntity()
                }
            )

            songEntities.addAll(
                remoteDetails.songs.map { song ->
                    song.toEntity()
                }
            )

            albumEntities.addAll(
                songAlbums.filter { album ->
                    album.id != playlistId // Avoid duplicating the main album if the playlist is an album
                }.map { it.toEntity() }.distinctBy { it.albumId }
            )

            albumArtistLinks.addAll(
                songAlbums.flatMap { album ->
                    album.artists.map { artist ->
                        AlbumArtistCrossRef(
                            albumId = album.id,
                            artistId = artist.id
                        )
                    }
                })

            artistsEntities.addAll(
                remoteDetails.songs.flatMap { it.artists }.map { it.toEntity() }
                    .distinctBy { it.artistId }
            )


            // 4. Call the single transaction method in the DAO
            db.withTransaction {

                logger.info { "Start DB transaction" }


                if (artistsEntities.isNotEmpty()) {
                    logger.info { "Inserting artists with IDs: ${artistsEntities.joinToString(", ") { it.artistId }}" }
                    db.artistDao().upsertAll(artistsEntities)
                }



                if (playlistEntities.isNotEmpty()) {
                    logger.info { "Inserting playlist with ID: ${playlistEntities.joinToString(", ") { it.playlistId }}" }
                    db.playlistDao().upsertAll(playlistEntities)
                }

                if (albumEntities.isNotEmpty()) {
                    logger.info {
                        "Inserting  albums with IDs: ${albumEntities.joinToString(", ") { it.albumId }}"
                    }
                    db.albumDao().upsertAll(albumEntities)
                }

                logger.info {
                    "Inserting songs with IDs: ${songEntities.joinToString(", ") { it.songId }} and albums IDs: ${
                        songEntities.joinToString(
                            ", "
                        ) { it.albumId ?: "N/A" }
                    }"
                }
                db.songDao().insertSongs(songEntities)

                db.albumDao().linkAlbumToArtists(albumArtistLinks)
                if (playlistSongsLinks.isNotEmpty()) {
                    logger.info { "Inserting playlist-song links for playlist ID: $playlistId" }
                    playlistSongsLinks.forEach { link ->
                        db.playlistDao().addSongToPlaylist(link)
                    }
                }

                logger.info { "End DB Transaction" }
            }

        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error on update data", e)
            e.printStackTrace()
        }
    }

}