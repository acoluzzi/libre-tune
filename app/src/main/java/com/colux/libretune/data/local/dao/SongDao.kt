package com.colux.libretune.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.colux.libretune.data.local.DatabaseConstants
import com.colux.libretune.data.local.entity.SongEntity
import com.colux.libretune.data.local.join.PlaylistSongCrossRef
import com.colux.libretune.data.local.join.SongArtistCrossRef
import com.colux.libretune.data.local.relation.SongWithArtistsAndAlbum
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun linkSongToArtists(crossRefs: List<SongArtistCrossRef>)


    // --- Query Methods ---
    @Transaction
    @Query("SELECT * FROM songs WHERE songId = :songId")
    fun getSongWithArtistsAndAlbum(songId: String): Flow<SongWithArtistsAndAlbum?>


    // --- Like / Dislike Logic ---
    // These methods interact with the Playlist-Song join table.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(join: PlaylistSongCrossRef)

    @Delete
    suspend fun removeSongFromPlaylist(join: PlaylistSongCrossRef)

    /**
     * "Likes" a song by adding it to the "Liked Songs" playlist.
     */
    suspend fun likeSong(songId: String) {
        val join = PlaylistSongCrossRef(
            playlistId = DatabaseConstants.LIKED_SONGS_PLAYLIST_ID,
            songId = songId
        )
        addSongToPlaylist(join)
    }

    /**
     * "Dislikes" a song by removing it from the "Liked Songs" playlist.
     */
    suspend fun unlikeSong(songId: String) {
        val join = PlaylistSongCrossRef(
            playlistId = DatabaseConstants.LIKED_SONGS_PLAYLIST_ID,
            songId = songId
        )
        removeSongFromPlaylist(join)
    }
}