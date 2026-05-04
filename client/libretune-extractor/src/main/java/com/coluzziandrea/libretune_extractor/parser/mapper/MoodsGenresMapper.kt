package com.coluzziandrea.libretune_extractor.parser.mapper

import com.coluzziandrea.libretune_extractor.client.response.BrowseData
import com.coluzziandrea.libretune_extractor.client.response.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.model.GenreMoodCategory
import com.coluzziandrea.libretune_extractor.model.GenreMoodCategoryPlaylistCarousel
import com.coluzziandrea.libretune_extractor.model.GenresMoods
import com.coluzziandrea.libretune_extractor.model.MoodGenreItem
import com.coluzziandrea.libretune_extractor.model.Playlist
import com.coluzziandrea.libretune_extractor.model.Song

fun BrowseData.toGenresMoods(): GenresMoods? {

    val genres = mutableListOf<MoodGenreItem>()
    val moods = mutableListOf<MoodGenreItem>()

    this.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.forEach { content ->
        if (content is SectionContent.GridContent) {
            val header =
                content.gridRenderer.header?.gridHeaderRenderer?.title?.runs?.firstOrNull()?.text?.lowercase()

            if (header?.lowercase()?.contains("genre") == true) {
                content.gridRenderer.items.forEach { item ->
                    val name =
                        item.musicNavigationButtonRenderer?.buttonText?.runs?.firstOrNull()?.text
                    val id =
                        item.musicNavigationButtonRenderer?.clickCommand?.browseEndpoint?.params
                    if (name != null && id != null) {
                        genres.add(MoodGenreItem(id = id, name = name))
                    }
                }
            } else if (header?.lowercase()?.contains("mood") == true) {
                content.gridRenderer.items.forEach { item ->
                    val name =
                        item.musicNavigationButtonRenderer?.buttonText?.runs?.firstOrNull()?.text
                    val id =
                        item.musicNavigationButtonRenderer?.clickCommand?.browseEndpoint?.params
                    if (name != null && id != null) {
                        moods.add(MoodGenreItem(id = id, name = name))
                    }
                }
            }
        }
    }

    return GenresMoods(
        genres = genres,
        moods = moods
    )
}


fun BrowseData.toGenreMoodCategory(): GenreMoodCategory? {
    val name =
        this.header?.musicHeaderRenderer?.title?.runs?.firstOrNull()?.text
    if (name == null) return null

    val songs = mutableListOf<Song>()
    val carousels = mutableListOf<GenreMoodCategoryPlaylistCarousel>()

    this.contents?.singleColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.forEach { content ->
        if (content is SectionContent.MusicCarouselContent) {
            val title =
                content.musicCarouselShelfRenderer.header.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.firstOrNull()?.text?.trim()

            if (title?.lowercase()?.contains("songs") == true) {
                content.musicCarouselShelfRenderer.contents.forEach { item ->
                    val song = item.musicResponsiveListItemRenderer?.toSong()
                    if (song != null) {
                        songs.add(song)
                    }
                }
            } else if (title?.lowercase()?.contains("videos") == false) {
                val playlists =
                    mutableListOf<Playlist>()
                content.musicCarouselShelfRenderer.contents.forEach { item ->
                    val playlist = item.musicTwoRowItemRenderer?.toPlaylist()
                    if (playlist != null) {
                        playlists.add(playlist)
                    }
                }
                if (playlists.isNotEmpty()) {
                    carousels.add(
                        GenreMoodCategoryPlaylistCarousel(
                            title = title,
                            playlists = playlists
                        )
                    )
                }
            }
        }
    }


    return GenreMoodCategory(
        name = name,
        songs = songs,
        carousels = carousels
    )
}