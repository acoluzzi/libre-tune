package com.colux.libretune.data.repository

import androidx.room.withTransaction
import com.colux.libretune.data.local.AppDatabase
import com.colux.libretune.data.local.DatabaseConstants
import com.colux.libretune.data.local.entity.PlaybackHistoryEntity
import com.colux.libretune.data.local.join.AlbumArtistCrossRef
import com.colux.libretune.data.local.join.PlaylistSongCrossRef
import com.colux.libretune.data.local.mapper.toDataModel
import com.colux.libretune.data.local.mapper.toEntity
import com.colux.libretune.data.model.Song
import com.colux.libretune.data.model.wrapper.SongWithAlbumAndArtists
import com.colux.libretune.data.remote.tube.YouTubeExtractionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepository @Inject constructor(
    private val remote: YouTubeExtractionRepository,
    private val db: AppDatabase,
) {
    suspend fun getSongUrlById(id: String): String? {
        return remote.getSongUrlById(id)
    }

    fun getSongById(id: String): Flow<Song?> {
        return db.songDao().getSongById(id).map { songEntity ->
            songEntity?.let { song ->
                val songAlbum = db.albumDao().getAlbumById(song.albumId ?: "")

                val albumArtists = songAlbum?.albumId?.let {
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
        return db.songDao().getSavedSongIds()
    }

    suspend fun logSongPlayed(song: Song) {
        db.historyDao().insertHistory(
            PlaybackHistoryEntity(
                songId = song.id,
                playedAtTimestamp = System.currentTimeMillis()
            )
        )
    }

    fun isSongLiked(id: String): Flow<Boolean> {
        return db.playlistDao().isSongInPlaylist(DatabaseConstants.LIKED_SONGS_PLAYLIST_ID, id)
    }

    fun isSongInPlaylist(playlistId: String, songId: String): Flow<Boolean> {
        return db.playlistDao().isSongInPlaylist(playlistId, songId)
    }

    suspend fun unlikeSong(song: Song) {
        val join = PlaylistSongCrossRef(
            playlistId = DatabaseConstants.LIKED_SONGS_PLAYLIST_ID,
            songId = song.id
        )
        db.playlistDao().removeSongFromPlaylist(join)
    }

    suspend fun likeSong(song: Song) {
        _addSongToPlaylist(DatabaseConstants.LIKED_SONGS_PLAYLIST_ID, song)
    }

    private suspend fun _addSongToPlaylist(playlistId: String, song: Song) {
        val albumEntity = song.album?.toEntity()
        val artistsEntities = song.artists.map { it.toEntity() }
        val songEntity = song.toEntity()

        val albumArtistsLinks = song.album?.let { album ->
            artistsEntities.map { artistEntity ->
                // Assuming you have an AlbumArtistCrossRef entity to represent the many-to-many relationship
                AlbumArtistCrossRef(
                    albumId = album.id,
                    artistId = artistEntity.artistId
                )
            }
        } ?: emptyList()

        db.withTransaction {
            db.artistDao().upsertAll(artistsEntities)

            if (albumEntity != null) {
                db.albumDao().upsert(albumEntity)
            }

            db.songDao().insertSong(songEntity)


            if (albumArtistsLinks.isNotEmpty()) {
                db.albumDao().linkAlbumToArtists(albumArtistsLinks)
            }

            val join = PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = song.id
            )
            db.playlistDao().addSongToPlaylist(join)
        }
    }

    suspend fun removeSongFromAllPlaylists(songId: String) {
        db.playlistDao().removeSongFromAllLocalPlaylists(songId)
    }

    suspend fun removeSongFromPlaylist(playlistId: String, songId: String) {
        db.playlistDao().removeSongFromPlaylist(PlaylistSongCrossRef(playlistId, songId))
    }

    suspend fun addSongToPlaylist(playlistId: String, song: Song) {
        _addSongToPlaylist(playlistId, song)
    }


}