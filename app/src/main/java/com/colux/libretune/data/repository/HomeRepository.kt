package com.colux.libretune.data.repository

import com.colux.libretune.data.local.AppDatabase
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.mapper.toDataModel
import com.colux.libretune.data.local.wrapper.PlaylistWithArtists
import com.colux.libretune.data.model.HomeFeedItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


data class ArtistWithRelatedArtists(
    val artist: ArtistEntity,
    val relatedArtists: List<ArtistEntity>
)

data class PlaylistWithRelatedPlaylists(
    val playlist: PlaylistWithArtists,
    val relatedPlaylists: List<PlaylistWithArtists>
)

@Singleton
class HomeRepository @Inject constructor(
    private val db: AppDatabase,
) {

    companion object {
        private const val MAX_ITEM_PER_SEED_COUNT = 5
    }

    private val logger = java.util.logging.Logger.getLogger(HomeRepository::class.java.name)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getHomeScreenFeed(): Flow<List<HomeFeedItem>> {
        // 1. Get flows from both history and the library
        val recentlyPlayedArtistsFlow =
            db.historyDao().getRecentlyPlayedArtists(MAX_ITEM_PER_SEED_COUNT)
        val savedArtistsFlow = db.libraryDao().getSavedArtists(MAX_ITEM_PER_SEED_COUNT)

        val recentlyPlayedAlbumsFlow =
            db.historyDao().getRecentlyPlayedAlbums(MAX_ITEM_PER_SEED_COUNT)
        val savedAlbumsFlow = db.libraryDao().getSavedPlaylists(MAX_ITEM_PER_SEED_COUNT)

        val seedArtistsFlow =
            combine(recentlyPlayedArtistsFlow, savedArtistsFlow) { played, saved ->
                (played + saved).distinctBy { it.artistId }.take(MAX_ITEM_PER_SEED_COUNT)
            }

        // 3. Combine the sources for albums
        val seedAlbumsFlow = combine(recentlyPlayedAlbumsFlow, savedAlbumsFlow) { played, saved ->
            (played + saved).distinctBy { it.playlist.playlistId }.take(MAX_ITEM_PER_SEED_COUNT)
        }

        val flowFinal = seedArtistsFlow.combine(seedAlbumsFlow) { artists, albums ->
            Pair(artists, albums)
        }.flatMapLatest { (artists, albums) ->
            logger.info { "Seed Artists: $artists" }
            logger.info { "Seed Albums: $albums" }

            if (artists.isEmpty() && albums.isEmpty()) {
                return@flatMapLatest flowOf(emptyList())
            }

            val relatedArtistsFlows = artists.map { artist ->
                db.artistDao().getSimilarArtists(artist.artistId).map { similarArtists ->

                    ArtistWithRelatedArtists(
                        artist = artist,
                        relatedArtists = similarArtists
                    )

                }
            }
            val relatedArtistsCombinedFlow = combine(relatedArtistsFlows) { artistFlow ->
                artistFlow.toList()
            }


            val relatedPlaylistsFlows = albums.map { album ->
                db.playlistDao().getRelatedPlaylistsWithArtists(album.playlist.playlistId)
                    .map { relatedPlaylists ->
                        PlaylistWithRelatedPlaylists(
                            playlist = album,
                            relatedPlaylists = relatedPlaylists
                        )
                    }
            }

            val relatedPlaylistsCombinedFlow = combine(relatedPlaylistsFlows) { playlistFlow ->
                playlistFlow.toList()
            }

            val combinedFlow =
                relatedArtistsCombinedFlow.combine(relatedPlaylistsCombinedFlow) { relatedArtists, relatedPlaylists ->
                    val homeFeedItemsFromArtist = relatedArtists.mapNotNull { ra ->
                        if (ra.relatedArtists.isNotEmpty()) {
                            HomeFeedItem.RelatedArtistsCarousel(
                                artist = ra.artist.toDataModel(),
                                artists = ra.relatedArtists.map { it.toDataModel() }
                            )
                        } else {
                            null
                        }
                    }

                    val homeFeedItemsFromPlaylists = relatedPlaylists.mapNotNull { rp ->
                        if (rp.relatedPlaylists.isNotEmpty()) {
                            HomeFeedItem.RelatedPlaylistsCarousel(
                                album = rp.playlist.playlist.toDataModel(),
                                playlists = rp.relatedPlaylists.map { it.toDataModel() }
                            )
                        } else {
                            null
                        }

                    }

                    (homeFeedItemsFromArtist + homeFeedItemsFromPlaylists).shuffled()
                }



            combinedFlow
        }


        return flowFinal
    }
}