package com.colux.libretune.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.colux.libretune.data.local.entity.AlbumEntity
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.SongEntity
import com.colux.libretune.data.local.join.AlbumArtistCrossRef
import com.colux.libretune.data.local.join.SongArtistCrossRef
import com.colux.libretune.data.local.relation.ArtistWithSongsAndAlbums
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    @Query("SELECT * FROM artists WHERE artistId = :artistId")
    suspend fun getArtist(artistId: String): ArtistEntity?

    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun linkAlbumsToArtist(crossRefs: List<AlbumArtistCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun linkSongsToArtist(crossRefs: List<SongArtistCrossRef>)

    /**
     * A single transaction to save all details for an artist.
     * This is called by the repository after a successful network fetch.
     */
    @Transaction
    suspend fun insertArtistDetails(details: ArtistWithSongsAndAlbums) {
        insertArtist(details.artist)
        insertAlbums(details.albums)
        insertSongs(details.songs)

        val albumArtistLinks = details.albums.map { album ->
            AlbumArtistCrossRef(albumId = album.albumId, artistId = details.artist.artistId)
        }
        linkAlbumsToArtist(albumArtistLinks)

        val songArtistLinks = details.songs.map { song ->
            SongArtistCrossRef(songId = song.songId, artistId = details.artist.artistId)
        }
        linkSongsToArtist(songArtistLinks)
    }

    /**
     * Fetches a single artist with their complete list of songs and albums.
     * @Transaction ensures Room runs all underlying queries together.
     */
    @Transaction
    @Query("SELECT * FROM artists WHERE artistId = :artistId")
    fun getArtistWithContent(artistId: String): Flow<ArtistWithSongsAndAlbums?>
}