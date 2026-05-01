package com.colux.libretune.data.repository

import androidx.room.withTransaction
import com.colux.libretune.data.local.AppDatabase
import com.colux.libretune.data.local.DatabaseConstants
import com.colux.libretune.data.local.entity.PlaybackHistoryEntity
import com.colux.libretune.data.local.join.HistoryArtistCrossRef
import com.colux.libretune.data.local.join.PlaylistArtistCrossRef
import com.colux.libretune.data.local.join.PlaylistSongCrossRef
import com.colux.libretune.data.local.join.SongArtistCrossRef
import com.colux.libretune.data.local.mapper.toDataModel
import com.colux.libretune.data.local.mapper.toEntity
import com.colux.libretune.data.model.Song
import com.colux.libretune.data.model.wrapper.SongWithAlbumAndArtists
import com.colux.libretune.data.remote.tube.YouTubeExtractionRepository
import com.colux.libretune.data.sync.SyncCollection
import com.colux.libretune.data.sync.SyncMetadataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepository @Inject constructor(
    private val remote: YouTubeExtractionRepository,
    private val db: AppDatabase,
    private val syncMetadata: SyncMetadataStore,
) {

    private fun collectionForPlaylist(playlistId: String): SyncCollection =
        if (playlistId == DatabaseConstants.LIKED_SONGS_PLAYLIST_ID) {
            SyncCollection.LIKED_SONGS
        } else {
            SyncCollection.PLAYLISTS
        }

    private val logger = java.util.logging.Logger.getLogger(SongRepository::class.java.name)

    suspend fun getSongUrlById(id: String): String? {
        return remote.getSongUrlById(id)
    }

    fun getSongById(id: String): Flow<Song?> {
        return db.songDao().getSongById(id).map { songEntity ->
            songEntity?.let { song ->
                val songAlbum = db.playlistDao().getPlaylistById(song.albumId ?: "")

                val albumArtists = songAlbum?.playlistId?.let {
                    db.artistDao().getArtistsByAlbumId(it)
                        .firstOrNull() ?: emptyList()
                } ?: emptyList()

                SongWithAlbumAndArtists(
                    song,
                    songAlbum,
                    albumArtists
                ).toDataModel()
            }
        }
    }

    fun getSavedSongIds(): Flow<List<String>> {
        return db.songDao().getSavedPlaylistSongIds().map {
            logger.info { "Emitting new saved songs: $it" }
            it
        }
    }

    suspend fun logSongPlayed(song: Song) {
        _saveSong(song)

        db.withTransaction {
            logger.info { "insertHistory for song played: $song" }
            db.historyDao().insertHistory(
                PlaybackHistoryEntity(
                    songId = song.id,
                    albumId = song.album?.id,
                    playedAtTimestamp = System.currentTimeMillis()
                )
            )

            db.historyDao().linkHistoryItemToArtists(
                song.artists.map { artist ->
                    HistoryArtistCrossRef(
                        historyId = db.historyDao().getLastInsertedId(),
                        artistId = artist.id
                    )
                }
            )
        }


    }

    fun isSongLiked(id: String): Flow<Boolean> {
        return db.playlistDao().isSongInPlaylist(DatabaseConstants.LIKED_SONGS_PLAYLIST_ID, id)
    }

    fun isSongInPlaylist(playlistId: String, songId: String): Flow<Boolean> {
        return db.playlistDao().isSongInPlaylist(playlistId, songId)
    }

    fun isSongInLocalPlaylist(playlistId: String, songId: String): Flow<Boolean> {
        return db.playlistDao().isSongInLocalPlaylist(playlistId, songId)
    }

    suspend fun unlikeSong(song: Song) {
        val join = PlaylistSongCrossRef(
            playlistId = DatabaseConstants.LIKED_SONGS_PLAYLIST_ID,
            songId = song.id
        )
        db.playlistDao().removeSongFromPlaylist(join)
        syncMetadata.setLocalChangedAt(SyncCollection.LIKED_SONGS)
    }

    suspend fun likeSong(song: Song) {
        logger.info { "Like song $song" }
        _addSongToPlaylist(DatabaseConstants.LIKED_SONGS_PLAYLIST_ID, song)
        syncMetadata.setLocalChangedAt(SyncCollection.LIKED_SONGS)
    }

    private suspend fun _saveSong(song: Song) {
        logger.info { "Save song $song" }
        val albumEntity = song.album?.toEntity()
        val artistsEntities = song.artists.map { it.toEntity() }
        val songEntity = song.toEntity()

        val albumArtistsLinks = song.album?.let { album ->
            artistsEntities.map { artistEntity ->
                PlaylistArtistCrossRef(
                    playlistId = album.id,
                    artistId = artistEntity.artistId
                )
            }
        } ?: emptyList()


        db.withTransaction {
            logger.info { "Upserting artist entities: $artistsEntities" }
            db.artistDao().upsertAll(artistsEntities)

            if (albumEntity != null) {
                logger.info { "Upserting album entities: $albumEntity" }
                db.playlistDao().upsert(albumEntity)
            }

            logger.info { "Inserting song: $songEntity" }
            db.songDao().insertSong(songEntity)


            if (albumArtistsLinks.isNotEmpty()) {
                logger.info { "Linking album to artist: $albumArtistsLinks" }
                db.playlistDao().linkAlbumToArtists(albumArtistsLinks)
            }

            val songArtistLinks = artistsEntities.map { artistEntity ->
                SongArtistCrossRef(
                    songId = song.id,
                    artistId = artistEntity.artistId
                )
            }
            logger.info { "Linking song to artist: $songArtistLinks" }
            db.songDao().linkSongsToArtists(songArtistLinks)
        }
    }

    private suspend fun _addSongToPlaylist(playlistId: String, song: Song) {
        _saveSong(song)

        val join = PlaylistSongCrossRef(
            playlistId = playlistId,
            songId = song.id
        )
        logger.info { "Linking song to playlist: $join" }
        db.playlistDao().addSongToPlaylist(join)
    }

    suspend fun removeSongFromAllPlaylists(songId: String) {
        db.playlistDao().removeSongFromAllLocalPlaylists(songId)
        syncMetadata.setLocalChangedAt(SyncCollection.LIKED_SONGS)
        syncMetadata.setLocalChangedAt(SyncCollection.PLAYLISTS)
    }

    suspend fun removeSongFromPlaylist(playlistId: String, songId: String) {
        db.playlistDao().removeSongFromPlaylist(PlaylistSongCrossRef(playlistId, songId))
        syncMetadata.setLocalChangedAt(collectionForPlaylist(playlistId))
    }

    suspend fun addSongToPlaylist(playlistId: String, song: Song) {
        _addSongToPlaylist(playlistId, song)
        syncMetadata.setLocalChangedAt(collectionForPlaylist(playlistId))
    }


}