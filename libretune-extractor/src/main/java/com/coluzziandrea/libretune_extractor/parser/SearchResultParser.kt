package com.coluzziandrea.libretune_extractor.parser

import com.coluzziandrea.libretune_extractor.client.response.BrowseData
import com.coluzziandrea.libretune_extractor.client.response.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.model.Artist
import com.coluzziandrea.libretune_extractor.model.GenericMusicItem
import com.coluzziandrea.libretune_extractor.model.Playlist
import com.coluzziandrea.libretune_extractor.model.SearchResult
import com.coluzziandrea.libretune_extractor.model.Song
import com.coluzziandrea.libretune_extractor.parser.mapper.toArtist
import com.coluzziandrea.libretune_extractor.parser.mapper.toPlaylist
import com.coluzziandrea.libretune_extractor.parser.mapper.toSong
import com.coluzziandrea.libretune_extractor.parser.mapper.toTopResult

class SearchResultParser {
    companion object {
        fun from(browseDataObject: BrowseData): SearchResult {

            val genericMusicItems = mutableListOf<GenericMusicItem>()
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
                                    genericMusicItems.add(topRes)
                                }
                            }

                            content.musicCardShelfRenderer.contents?.forEach { subItem ->
                                if (subItem is SectionContent.MusicResponsiveListItemContent) {
                                    val songGenericMusicItem = GenericMusicItem.SongResult(
                                        song = subItem.musicResponsiveListItemRenderer.toSong()
                                    )
                                    genericMusicItems.add(songGenericMusicItem)
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
                                            shelfItem.musicResponsiveListItemRenderer.toSong().let {
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
                                            shelfItem.musicResponsiveListItemRenderer.toArtist()
                                                .let {
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
                genericMusicItems = genericMusicItems,
                songs = songs,
                albums = albums,
                artists = artists,
                playlists = playlists,
                communityPlaylists = communityPlaylists
            )
        }
    }
}