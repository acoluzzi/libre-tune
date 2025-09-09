package com.colux.libretune.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.colux.libretune.data.local.AppDatabase
import com.colux.libretune.data.local.entity.AlbumType
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.LibraryEntity
import com.colux.libretune.data.local.entity.LibraryItemType
import com.colux.libretune.data.local.entity.PlaylistEntity
import com.colux.libretune.data.local.entity.SongEntity
import com.colux.libretune.data.local.join.PlaylistArtistCrossRef
import com.colux.libretune.data.local.join.PlaylistRelatedCrossRef
import com.colux.libretune.data.local.join.PlaylistSongCrossRef
import com.colux.libretune.data.local.join.SongArtistCrossRef
import com.colux.libretune.data.local.mapper.toDataModel
import com.colux.libretune.data.local.mapper.toEntity
import com.colux.libretune.data.local.wrapper.PlaylistWithArtists
import com.colux.libretune.data.local.wrapper.PlaylistWithSongsEntity
import com.colux.libretune.data.local.wrapper.SongWithAlbumAndArtist
import com.colux.libretune.data.model.PlaylistDetails
import com.colux.libretune.data.model.PlaylistType
import com.colux.libretune.data.model.wrapper.PlaylistWithSongs
import com.colux.libretune.data.model.wrapper.SongWithAlbumAndArtists
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
    companion object {
        private const val MAX_RELATED_FOR_PLAYLIST = 10
    }

    private val logger = Logger.getLogger("PlaylistRepository")

    private val cacheTtlMillis = TimeUnit.SECONDS.toMillis(1)


    @OptIn(ExperimentalCoroutinesApi::class)
    fun getSavedPlaylistsWithSongs(): Flow<List<PlaylistWithSongs>> {
        val playlistFlow: Flow<List<PlaylistWithSongsEntity>> = db.playlistDao().getSavedPlaylists()

        return playlistFlow.map { playlists ->
            playlists.map { (playlist, songs, artists) ->
                PlaylistWithSongs(
                    playlist = playlist.toDataModel(artists),
                    songs = songs.map { song ->
                        val songAlbum = song.albumId?.let { db.playlistDao().getPlaylistById(it) }
                        val albumArtists = songAlbum?.playlistId?.let {
                            db.artistDao().getArtistsByAlbumId(it).firstOrNull() ?: emptyList()
                        } ?: emptyList()

                        SongWithAlbumAndArtists(
                            song,
                            songAlbum,
                            albumArtists
                        ).toDataModel()
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getLocalPlaylistsWithSongs(): Flow<List<PlaylistWithSongs>> {
        val playlistFlow: Flow<List<PlaylistWithSongsEntity>> = db.playlistDao().getLocalPlaylists()

        return playlistFlow.map { playlists ->
            playlists.map { (playlist, songs) ->
                PlaylistWithSongs(
                    playlist = playlist.toDataModel(),
                    songs = songs.map { song ->
                        val songAlbum = song.albumId?.let { db.playlistDao().getPlaylistById(it) }
                        val albumArtists = songAlbum?.playlistId?.let {
                            db.artistDao().getArtistsByAlbumId(it).firstOrNull() ?: emptyList()
                        } ?: emptyList()

                        SongWithAlbumAndArtists(
                            song,
                            songAlbum,
                            albumArtists
                        ).toDataModel()
                    }
                )
            }
        }
    }

    suspend fun createNewPlaylist(name: String) {
        val newPlaylist = PlaylistEntity(
            playlistId = "local-${System.currentTimeMillis()}",
            name = name,
            images = emptyList(),
            isLocal = true,
            type = AlbumType.PLAYLIST,
            updateTimestamp = System.currentTimeMillis()
        )
        db.playlistDao().insert(newPlaylist)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    fun getPlaylistDetails(id: String): Flow<PlaylistDetails?> {
        logger.info { "getPlaylistDetails() Starting getPlaylistDetails for $id" }


        // Start with the main playlist/album flow. This is our root.
        return db.playlistDao().getPlaylistWithArtists(id).flatMapLatest { playlistWithArtists ->
            // If the main entity is null, we can't proceed.
            if (playlistWithArtists == null) {
                logger.info { "getPlaylistDetails() Playlist $id not found in DB." }
                return@flatMapLatest flowOf(null)
            }

            // Now that we have the main playlist, create flows for all its related data.
            val songsFromPlaylistFlow =
                db.songDao()
                    .getSongsWithAlbumAndArtistByPlaylistId(playlistWithArtists.playlist.playlistId)

            val songsFromAlbumFlow = db.songDao()
                .getSongsWithAlbumAndArtistByAlbumId(playlistWithArtists.playlist.playlistId)

            val songsFlow: Flow<List<SongWithAlbumAndArtist>> =
                combine(songsFromAlbumFlow, songsFromPlaylistFlow) { fromAlbum, fromPlaylist ->
                    (fromAlbum + fromPlaylist).distinctBy { it.song.songId }
                }

            val relatedPlaylistsFlow: Flow<List<PlaylistWithArtists>> =
                db.playlistDao()
                    .getRelatedPlaylistsWithArtists(
                        playlistWithArtists.playlist.playlistId,
                        MAX_RELATED_FOR_PLAYLIST // Limit the number of related playlists - as this will be greater every time we fetch from remote
                    )



            logger.info { "getPlaylistDetails() building combined Flow for $id" }
            // Combine all the related data flows.
            combine(
                songsFlow,
                relatedPlaylistsFlow
            ) { songs, related ->
                logger.info { "getPlaylistDetails() Combining flows for $id" }

                // Build the final, clean UI model.
                PlaylistDetails(
                    id = playlistWithArtists.playlist.playlistId,
                    name = playlistWithArtists.playlist.name,
                    images = playlistWithArtists.playlist.images.map { it.toDataModel() },
                    type = when (playlistWithArtists.playlist.type) {
                        AlbumType.ALBUM -> PlaylistType.ALBUM
                        AlbumType.SINGLE -> PlaylistType.SINGLE
                        AlbumType.EP -> PlaylistType.EP
                        AlbumType.PLAYLIST -> PlaylistType.PLAYLIST
                    },
                    artists = playlistWithArtists.artists.map { it.toDataModel() },
                    songs = songs.map { it.toDataModel() }.sortedBy { it.trackNumber },
                    relatedPlaylists = related.map { it.toDataModel() },
                    isLocal = playlistWithArtists.playlist.isLocal ?: false,
                    releaseYear = playlistWithArtists.playlist.releaseYear ?: 0
                )
            }
        }
    }


    private suspend fun shouldFetch(id: String): Boolean {
        // Fetch the artist record just to check its timestamp
        val cachedPlaylistObj = db.playlistDao().getPlaylistById(id)

        // Always fetch if there's no data
        if (cachedPlaylistObj == null) return true

        if (cachedPlaylistObj.isLocal == true) {
            logger.info { "getPlaylistDetails() Playlist $id is local, skipping remote fetch." }
            return false
        }

        val lastUpdate = cachedPlaylistObj.updateTimestamp ?: 0
        if (lastUpdate == 0L) return true

        // Fetch if the data is older than our TTL
        val isStale =
            (System.currentTimeMillis() - lastUpdate) > cacheTtlMillis
        return isStale
    }

    suspend fun savePlaylist(playlistId: String) {
        db.libraryDao().insert(
            LibraryEntity(
                id = playlistId,
                type = LibraryItemType.PLAYLIST,
                playlistId = playlistId,
                addedAtTimestamp = System.currentTimeMillis()
            )
        )
    }

    fun isPlaylistSaved(playlistId: String): Flow<Boolean> {
        return db.libraryDao().isItemInLibrary(playlistId, LibraryItemType.PLAYLIST)
    }

    suspend fun unsavePlaylist(playlistId: String) {
        db.libraryDao().delete(
            LibraryEntity(
                id = playlistId,
                type = LibraryItemType.PLAYLIST,
                playlistId = playlistId,
                addedAtTimestamp = System.currentTimeMillis()
            )
        )
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
            val albumEntities = mutableListOf<PlaylistEntity>()
            val songEntities = mutableListOf<SongEntity>()
            val playlistEntities = mutableListOf<PlaylistEntity>()
            val albumArtistLinks = mutableListOf<PlaylistArtistCrossRef>()
            val playlistSongsLinks = mutableListOf<PlaylistSongCrossRef>()
            val playlistRelatedLinks = mutableListOf<PlaylistRelatedCrossRef>()
            val songArtistLinks = mutableListOf<SongArtistCrossRef>()


            // --- MAPPING LOGIC ---
            when (remoteDetails.type) {
                PlaylistType.ALBUM, PlaylistType.SINGLE, PlaylistType.EP -> {
                    logger.info { "Mapping as AlbumEntity" }
                    albumEntities.add(
                        PlaylistEntity(
                            playlistId = playlistId,
                            name = remoteDetails.name,
                            images = remoteDetails.images.map {
                                it.toEntity()
                            },
                            type = if (remoteDetails.type == PlaylistType.ALBUM) AlbumType.ALBUM else if (remoteDetails.type == PlaylistType.SINGLE) AlbumType.SINGLE else AlbumType.EP,
                            releaseYear = remoteDetails.releaseYear,
                            updateTimestamp = System.currentTimeMillis()
                        )
                    )

                    albumEntities.addAll(
                        remoteDetails.relatedPlaylists.map {
                            it.toEntity()
                        }
                    )

                    artistsEntities.addAll(
                        remoteDetails.relatedPlaylists.map {
                            it.artists
                        }.flatten().map {
                            Log.d(
                                "PlaylistRepositoryDebug",
                                "Adding Related playlist artist: $it"
                            )
                            it.toEntity()
                        }
                    )

                    playlistRelatedLinks.addAll(
                        remoteDetails.relatedPlaylists.map { related ->
                            Log.d(
                                "PlaylistRepositoryDebug",
                                "Linking album $playlistId to related playlist ${related.name} with ID ${related.id}"
                            )
                            Log.d(
                                "PlaylistRepositoryDebug", "$related"
                            )
                            PlaylistRelatedCrossRef(
                                parentPlaylistId = playlistId,
                                relatedPlaylistId = related.id
                            )
                        }
                    )

                    albumArtistLinks.addAll(
                        remoteDetails.relatedPlaylists.flatMap { playlist ->
                            playlist.artists.map { artist ->
                                PlaylistArtistCrossRef(
                                    playlistId = playlist.id,
                                    artistId = artist.id
                                )
                            }
                        }
                    )
                }

                PlaylistType.PLAYLIST -> {

                    logger.info { "Mapping as PlaylistEntity" }
                    playlistEntities.add(
                        PlaylistEntity(
                            playlistId = playlistId,
                            name = remoteDetails.name,
                            images = remoteDetails.images.map {
                                it.toEntity()
                            },
                            updateTimestamp = System.currentTimeMillis(),
                            isLocal = false,
                            type = AlbumType.PLAYLIST,
                        )
                    )

                    playlistEntities.addAll(
                        remoteDetails.relatedPlaylists.map {
                            it.toEntity()
                        }
                    )

                    playlistSongsLinks.addAll(
                        remoteDetails.songs.map { song ->
                            PlaylistSongCrossRef(
                                playlistId = playlistId,
                                songId = song.id
                            )
                        }
                    )

                    playlistRelatedLinks.addAll(
                        remoteDetails.relatedPlaylists.map { related ->
                            Log.d(
                                "PlaylistRepositoryDebug",
                                "Linking playlist $playlistId to related playlist ${related.name} with ID ${related.id}"
                            )
                            PlaylistRelatedCrossRef(
                                parentPlaylistId = playlistId,
                                relatedPlaylistId = related.id
                            )
                        }
                    )
                }
            }

            val songAlbums = remoteDetails.songs.mapNotNull {
                it.album
            }.distinctBy { it.id }

            logger.info { "Songs albums size: ${songAlbums.size}" }

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
                }.map { it.toEntity() }.distinctBy { it.playlistId }
            )

            albumArtistLinks.addAll(
                songAlbums.flatMap { album ->
                    album.artists.map { artist ->
                        PlaylistArtistCrossRef(
                            playlistId = album.id,
                            artistId = artist.id
                        )
                    }
                })

            songArtistLinks.addAll(
                remoteDetails.songs.flatMap { song ->
                    song.artists.map { artist ->
                        SongArtistCrossRef(
                            songId = song.id,
                            artistId = artist.id
                        )
                    }
                }
            )

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
                    logger.info { "Inserting playlists with ID: ${playlistEntities.joinToString(", ") { it.playlistId }}" }
                    db.playlistDao().upsertAll(playlistEntities)
                }

                logger.info { "Albums size: ${albumEntities.size}" }
                if (albumEntities.isNotEmpty()) {
                    logger.info {
                        "Inserting  albums with IDs: ${albumEntities.joinToString(", ") { it.playlistId }}"
                    }
                    db.playlistDao().upsertAll(albumEntities)
                }

                db.songDao().insertSongs(songEntities)

                db.playlistDao().linkAlbumToArtists(albumArtistLinks)

                if (playlistSongsLinks.isNotEmpty()) {
                    logger.info { "Inserting playlist-song links for playlist ID: $playlistId" }
                    playlistSongsLinks.forEach { link ->
                        db.playlistDao().addSongToPlaylist(link)
                    }
                }

                if (playlistRelatedLinks.isNotEmpty()) {
                    Log.d(
                        "PlaylistRepositoryDebug",
                        "Inserting ${playlistRelatedLinks.size} playlist-related links for playlist ID: $playlistId"
                    )
                    logger.info { "Inserting ${playlistRelatedLinks.size} playlist-related links for playlist ID: $playlistId" }
                    db.playlistDao().linkPlaylistsToRelatedPlaylists(playlistRelatedLinks)
                }


                if (songArtistLinks.isNotEmpty()) {
                    logger.info { "Inserting song-artist links for playlist ID: $playlistId" }
                    db.songDao().linkSongsToArtists(songArtistLinks)
                }

                logger.info { "End DB Transaction" }
            }

        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error on update data", e)
            e.printStackTrace()
        }
    }


}