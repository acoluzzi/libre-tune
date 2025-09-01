package com.coluzziandrea.libretune_extractor.parser

import com.coluzziandrea.libretune_extractor.browse_response.BrowseData
import com.coluzziandrea.libretune_extractor.browse_response.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.model.Artist
import com.coluzziandrea.libretune_extractor.model.Playlist
import com.coluzziandrea.libretune_extractor.model.SearchResult
import com.coluzziandrea.libretune_extractor.model.Song
import com.coluzziandrea.libretune_extractor.model.TopResult
import com.coluzziandrea.libretune_extractor.parser.mapper.toPlaylist
import com.coluzziandrea.libretune_extractor.parser.mapper.toTopResult

class SearchResultParser {
    companion object {
        fun from(browseDataObject: BrowseData): SearchResult {

            val topResults = mutableListOf<TopResult>()
            val songs = mutableListOf<Song>()
            val albums = mutableListOf<Playlist>()
            val artists = mutableListOf<Artist>()
            val playlists = mutableListOf<Playlist>()
            val communityPlaylists = mutableListOf<Playlist>()

            browseDataObject.contents.tabbedSearchResultsRenderer?.tabs?.forEach { tab ->
                tab.tabRenderer.content.sectionListRenderer.contents.forEach { content ->

                    when (content) {

                        is SectionContent.MusicCardShelfContent -> {

                            content.toTopResult().let { topRes ->
                                if (topRes != null) {
                                    topResults.add(topRes)
                                }
                            }

                            content.musicCardShelfRenderer.contents?.forEach { subItem ->
                                if (subItem is SectionContent.MusicResponsiveListItemContent) {
                                    val songTopResult = TopResult.SongResult(
                                        song = Song.from(subItem)
                                    )
                                    topResults.add(songTopResult)
                                }
                            }

                        }

                        is SectionContent.MusicShelfContent -> {

                            val header = content.musicShelfRenderer.title?.runs?.firstOrNull()?.text

                            when (header) {

                                "Albums" -> {
                                    content.musicShelfRenderer.contents.forEach { shelfItem ->
                                        shelfItem.toPlaylist().let {
                                            if (it != null) {
                                                albums.add(it)
                                            }
                                        }
                                    }
                                }


                                "Songs" -> {
                                    content.musicShelfRenderer.contents.forEach { shelfItem ->
                                        if (shelfItem is SectionContent.MusicResponsiveListItemContent) {
                                            Song.from(shelfItem).let {
                                                if (it != null) {
                                                    songs.add(it)
                                                }
                                            }
                                        }
                                    }
                                }

                                "Artists" -> {
                                    content.musicShelfRenderer.contents.forEach { shelfItem ->
                                        if (shelfItem is SectionContent.MusicResponsiveListItemContent) {
                                            Artist.from(shelfItem).let {
                                                if (it != null) {
                                                    artists.add(it)
                                                }
                                            }
                                        }
                                    }
                                }

                                "Featured playlists" -> {
                                    content.musicShelfRenderer.contents.forEach { shelfItem ->
                                        shelfItem.toPlaylist().let {
                                            if (it != null) {
                                                playlists.add(it)
                                            }
                                        }
                                    }
                                }

                                "Community playlists" -> {
                                    content.musicShelfRenderer.contents.forEach { shelfItem ->
                                        shelfItem.toPlaylist().let {
                                            if (it != null) {
                                                communityPlaylists.add(it)
                                            }
                                        }
                                    }
                                }

                                else -> {}
                            }

                        }


                        else -> {}
                    }
                }
            }

            return SearchResult(
                topResults = topResults,
                songs = songs,
                albums = albums,
                artists = artists,
                playlists = playlists,
                communityPlaylists = communityPlaylists
            )
        }
    }
}