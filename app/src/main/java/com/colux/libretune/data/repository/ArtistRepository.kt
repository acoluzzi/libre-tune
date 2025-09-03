package com.colux.libretune.data.repository

import androidx.room.withTransaction
import com.colux.libretune.data.local.AppDatabase
import com.colux.libretune.data.local.entity.AlbumEntity
import com.colux.libretune.data.local.entity.AlbumType
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.SongEntity
import com.colux.libretune.data.local.join.AlbumArtistCrossRef
import com.colux.libretune.data.local.join.ArtistArtistCrossRef
import com.colux.libretune.data.local.mapper.toDataModel
import com.colux.libretune.data.local.mapper.toEntity
import com.colux.libretune.data.local.wrapper.AlbumWithArtists
import com.colux.libretune.data.local.wrapper.SongWithAlbumAndArtists
import com.colux.libretune.data.model.ArtistDetails
import com.colux.libretune.data.remote.tube.YouTubeExtractionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import java.util.concurrent.TimeUnit
import java.util.logging.Level
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
    private val cacheTtlMillis = TimeUnit.MINUTES.toMillis(60)

    fun getArtistDetails(artistId: String): Flow<ArtistDetails?> {

        logger.info { "Starting getArtistDetails for $artistId" }

        // 1. Get the individual flows from each DAO.
        val artistFlow: Flow<ArtistEntity?> = db.artistDao().getArtist(artistId)
        val albumsFlow: Flow<List<AlbumEntity>> = db.albumDao().getAlbumsByArtistId(artistId)
        val singlesFlow: Flow<List<AlbumEntity>> = db.albumDao().getSinglesByArtistId(artistId)
        val similarArtistsFlow: Flow<List<ArtistEntity>> =
            db.artistDao().getSimilarArtists(artistId)

        val albumsAndSinglesFlow = combine(albumsFlow, singlesFlow) { albums, singles ->
            albums + singles
        }

        val songsFlow: Flow<List<SongEntity>> = albumsAndSinglesFlow.map { albums ->
            albums.flatMap { album ->
                db.songDao().getSongsByAlbumId(album.albumId)
            }
        }


        val songsWithAlbumsAndArtistsFlow: Flow<List<SongWithAlbumAndArtists>> =
            songsFlow.mapNotNull { songs ->
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

        val albumsWithArtistsFlow: Flow<List<AlbumWithArtists>> =
            albumsAndSinglesFlow.map { albums ->
                albums.map { album ->
                    val albumArtists =
                        db.artistDao().getArtistsByAlbumId(album.albumId).firstOrNull()
                    AlbumWithArtists(
                        album,
                        albumArtists ?: emptyList()
                    )
                }
            }


        // 2. Use 'combine' to merge the results from all flows.
        return combine(
            artistFlow,
            songsWithAlbumsAndArtistsFlow,
            albumsWithArtistsFlow,
            similarArtistsFlow
        ) { artist, songs, albums, similarArtists ->
            // This block runs whenever any of the source flows emit a new value.
            // If the main artist doesn't exist, we can't build the details.
            if (artist == null) return@combine null

            // 3. Manually "stitch" the data together into the clean UI model.
            ArtistDetails(
                name = artist.name,
                description = artist.description,
                images = artist.images.map { it.toDataModel() },
                topSongs = songs.map {
                    it.toDataModel()
                },
                albums = albums.filter {
                    it.albumEntity.type == AlbumType.ALBUM
                }.map {
                    it.toDataModel()
                },
                singlesAndEPs = albums.filter {
                    it.albumEntity.type == AlbumType.SINGLE_EP
                }.map {
                    it.toDataModel()
                },
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

        val lastUpdate = cachedArtist.updateTimestamp ?: 0
        if (lastUpdate == 0L) return true

        // Fetch if the data is older than our TTL
        val isStale =
            (System.currentTimeMillis() - lastUpdate) > cacheTtlMillis
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

            logger.info { "Received data from remote: $remoteDetails" }

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


            // 2. Map the lists of other entities
            val songEntities = remoteDetails.topSongs.map { it.toEntity() }
            val songAlbums = remoteDetails.topSongs.mapNotNull {
                it.album
            }
            val songArtists = remoteDetails.topSongs.flatMap {
                it.artists
            }

            val albums =
                (remoteDetails.albums + remoteDetails.singlesAndEPs).distinctBy {
                    it.id
                }

            val songAlbumsDiff = songAlbums.filter {
                albums.none { ae -> ae.id == it.id }
            }

            val albumEntities = (albums + songAlbumsDiff).map { it.toEntity() }

            val artistsEntities = (remoteDetails.similarArtists + songArtists).distinctBy {
                it.id
            }.filter {
                it.id != artistId
            }.map {
                it.toEntity()
            }

            val albumArtistLinks =
                (remoteDetails.albums + remoteDetails.singlesAndEPs + songAlbums).flatMap { album ->
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


            // 4. Call the single transaction method in the DAO
            db.withTransaction {

                logger.info { "Start DB transaction" }

                logger.info { "Inserting artist with ID: ${artistEntity.artistId}" }
                db.artistDao().upsert(artistEntity)


                logger.info { "Inserting  artists with IDs: ${artistsEntities.joinToString(", ") { it.artistId }}" }
                db.artistDao().upsertAll(artistsEntities)


                logger.info {
                    "Inserting  albums with IDs: ${albumEntities.joinToString(", ") { it.albumId }}"
                }
                db.albumDao().upsertAll(albumEntities)


                logger.info {
                    "Inserting songs with IDs: ${songEntities.joinToString(", ") { it.songId }} and albums IDs: ${
                        songEntities.joinToString(
                            ", "
                        ) { it.albumId ?: "N/A" }
                    }"
                }
                db.songDao().insertSongs(songEntities)

                db.albumDao().linkAlbumToArtists(albumArtistLinks)
                db.artistDao().linkSimilarArtists(similarArtistLinks)

                logger.info { "End DB Transaction" }
            }

        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error on update data", e)
            e.printStackTrace()
        }
    }


}