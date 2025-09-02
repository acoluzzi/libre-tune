package com.colux.libretune.data.repository

import androidx.room.withTransaction
import com.colux.libretune.data.local.AppDatabase
import com.colux.libretune.data.local.entity.AlbumEntity
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.SongEntity
import com.colux.libretune.data.local.join.AlbumArtistCrossRef
import com.colux.libretune.data.local.join.ArtistArtistCrossRef
import com.colux.libretune.data.local.join.SongArtistCrossRef
import com.colux.libretune.data.local.mapper.toDataModel
import com.colux.libretune.data.local.mapper.toEntity
import com.colux.libretune.data.model.ArtistDetails
import com.colux.libretune.data.remote.tube.YouTubeExtractionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtistRepository @Inject constructor(
    private val remote: YouTubeExtractionRepository,
    private val db: AppDatabase,
) {

    private val logger = Logger.getLogger("ArtistRepository")

    // Define how long the cache should be valid (e.g., 60 minutes)
    private val cacheTtlMillis = TimeUnit.SECONDS.toMillis(1) //TODO

    fun getArtistDetails(artistId: String): Flow<ArtistDetails?> {

        logger.info { "Starting getArtistDetails for $artistId" }

        // 1. Get the individual flows from each DAO.
        val artistFlow: Flow<ArtistEntity?> = db.artistDao().getArtist(artistId)
        val songsFlow: Flow<List<SongEntity>> = db.songDao().getSongsForArtist(artistId)
        val albumsFlow: Flow<List<AlbumEntity>> = db.albumDao().getAlbumsByArtistId(artistId)
        val singlesFlow: Flow<List<AlbumEntity>> = db.albumDao().getSinglesByArtistId(artistId)
        val similarArtistsFlow: Flow<List<ArtistEntity>> =
            db.artistDao().getSimilarArtists(artistId)

        // 2. Use 'combine' to merge the results from all flows.
        return combine(
            artistFlow,
            songsFlow,
            albumsFlow,
            singlesFlow,
            similarArtistsFlow
        ) { artist, songs, albums, singles, similarArtists ->
            // This block runs whenever any of the source flows emit a new value.
            // If the main artist doesn't exist, we can't build the details.
            logger.info { "Resolving combined artist flow $artist" }
            if (artist == null) return@combine null

            logger.info { "mapping final entity" }
            // 3. Manually "stitch" the data together into the clean UI model.
            ArtistDetails(
                name = artist.name,
                description = artist.description,
                images = artist.images.map { it.toDataModel() },
                topSongs = emptyList(),
                albums = emptyList(),
                singlesAndEPs = emptyList(),
                similarArtists = similarArtists.map { it.toDataModel() },
                featuring = emptyList(),
                playlists = emptyList()
            )
        }
    }


    private suspend fun shouldFetch(artistId: String): Boolean {
        // Fetch the artist record just to check its timestamp
        val cachedArtist = db.artistDao().getArtist(artistId).firstOrNull()

        // Always fetch if there's no data
        if (cachedArtist == null) return true

        // Fetch if the data is older than our TTL
        val isStale =
            (System.currentTimeMillis() - cachedArtist.updateTimestamp) > cacheTtlMillis
        return isStale
    }

    suspend fun refreshArtistDetails(artistId: String) {
        if (shouldFetch(artistId)) {
            logger.info { "Fetching data from remote" }
            updateArtistDetailsFromRemote(artistId)
        }
    }

    private suspend fun updateArtistDetailsFromRemote(artistId: String) {
        try {

            val remoteDetails = remote.getArtistDetails(artistId) ?: return

            logger.info { "Received data from remote" }
            // --- MAPPING LOGIC ---
            // 1. Map the main artist
            val artistEntity = ArtistEntity(
                artistId = artistId,
                name = remoteDetails.name,
                images = remoteDetails.images.map {
                    it.toEntity()
                },
                updateTimestamp = System.currentTimeMillis()
            )

            logger.info { "built artistEntity $artistEntity" }

            // 2. Map the lists of other entities
//            val songEntities = remoteDetails.topSongs.map { it.toEntity() }
//            val albumEntities = remoteDetails.albums.map { it.toEntity() }
            val similarArtists = remoteDetails.similarArtists.map { it.toEntity() }

            // 3. Create the links for the join tables
            val songArtistLinks = remoteDetails.topSongs.flatMap { song ->
                song.artists.map { artist ->
                    SongArtistCrossRef(
                        songId = song.id,
                        artistId = artist.id
                    )
                }
            }
            val albumArtistLinks = remoteDetails.albums.flatMap { album ->
                album.artists.map { artist ->
                    AlbumArtistCrossRef(
                        albumId = album.id,
                        artistId = artist.id
                    )
                }
            }
            val similarArtistLinks = remoteDetails.similarArtists.map { similar ->
                ArtistArtistCrossRef(parentArtistId = artistId, relatedArtistId = similar.id)
            }


            logger.info { "Built children entities" }

            // 4. Call the single transaction method in the DAO
            db.withTransaction {

                logger.info { "Start DB transaction" }
                db.artistDao().insertArtist(artistEntity)
                db.songDao().insertSongs(emptyList())
                db.albumDao().insertAlbums(emptyList())
                db.artistDao().insertArtists(similarArtists)

                db.songDao().linkSongToArtists(songArtistLinks)
                db.albumDao().linkAlbumToArtists(albumArtistLinks)
                db.artistDao().linkSimilarArtists(similarArtistLinks)

                logger.info { "End DB Transaction" }
            }

        } catch (e: Exception) {
            logger.warning { "Error on update Data" }
            e.printStackTrace()
        }
    }


}