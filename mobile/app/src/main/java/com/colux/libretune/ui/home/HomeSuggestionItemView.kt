package com.colux.libretune.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.colux.libretune.data.model.HomeFeedItem
import com.colux.libretune.ui.components.artist.ArtistCarousel
import com.colux.libretune.ui.components.playlist.PlaylistCarousel
import com.colux.libretune.ui.nav.Screen

@Composable
fun HomeSuggestionItemView(item: HomeFeedItem, navController: NavController) {
    when (item) {
        is HomeFeedItem.RelatedArtistsCarousel -> {
            ArtistCarousel(
                relatedToArtist = item.artist,
                artists = item.artists,
                onItemClick = { artistId ->
                    navController.navigate(Screen.Artist.createRoute(artistId))
                },
                onRelatedArtistClick = {
                    navController.navigate(Screen.Artist.createRoute(item.artist.id))
                },
                modifier = Modifier
            )
        }

        is HomeFeedItem.RelatedPlaylistsCarousel -> {
            PlaylistCarousel(
                playlists = item.playlists,
                onItemClick = { index ->
                    val playlistId =
                        item.playlists.getOrNull(index)?.id ?: return@PlaylistCarousel
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                },
                modifier = Modifier,
                relatedPlaylist = item.album,
                onRelatedPlaylistClick = { playlistId ->
                    navController.navigate(Screen.PlaylistDetail.createRoute(playlistId))
                }
            )
        }
    }
}

