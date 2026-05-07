package com.coluzziandrea.libretune_extractor

import com.coluzziandrea.libretune_extractor.client.LibreClient
import com.coluzziandrea.libretune_extractor.model.ArtistDetails
import com.coluzziandrea.libretune_extractor.model.Discography
import com.coluzziandrea.libretune_extractor.model.GenreMoodCategory
import com.coluzziandrea.libretune_extractor.model.GenresMoods
import com.coluzziandrea.libretune_extractor.model.PlaylistDetails
import com.coluzziandrea.libretune_extractor.model.SearchResult
import com.coluzziandrea.libretune_extractor.model.SearchSuggestion
import com.coluzziandrea.libretune_extractor.parser.ArtistParser
import com.coluzziandrea.libretune_extractor.parser.PlaylistParser
import com.coluzziandrea.libretune_extractor.parser.SearchResultParser
import com.coluzziandrea.libretune_extractor.parser.mapper.toDiscography
import com.coluzziandrea.libretune_extractor.parser.mapper.toGenreMoodCategory
import com.coluzziandrea.libretune_extractor.parser.mapper.toGenresMoods
import com.coluzziandrea.libretune_extractor.parser.toSearchSuggestions
import io.ktor.client.HttpClient

class LibreTuneExtractor(
    httpClient: HttpClient
) {

    companion object {
        const val GENRES_MOODS_BROWSE_ID = "FEmusic_moods_and_genres"
        const val GENRE_MOOD_CATEGORY_BROWSE_ID = "FEmusic_moods_and_genres_category"
    }

    private val client = LibreClient(httpClient)


    suspend fun playlist(playlistId: String): PlaylistDetails? {
        return client.browse(playlistId).let {
            PlaylistParser.from(it, playlistId)
        }
    }


    suspend fun artist(channelId: String): ArtistDetails? {
        return client.browse(channelId).let {
            ArtistParser.from(it, channelId)
        }
    }

    // YouTube Music's /youtubei/v1/search expects the query verbatim inside the
    // JSON body; the previous URLEncoder.encode / encodeURLParameter wrappers
    // double-encoded multi-word queries (e.g. "Bohemian Rhapsody" became
    // "Bohemian%20Rhapsody"), causing the server to return no results.
    suspend fun search(query: String): SearchResult? {
        return client.search(query).let {
            SearchResultParser.from(it)
        }
    }


    suspend fun searchSuggestions(query: String): List<SearchSuggestion> {
        return client.searchSuggestions(query)
            .toSearchSuggestions()
    }

    suspend fun discography(id: String, params: String? = null): Discography? {
        return client.browse(id, params).toDiscography()
    }

    suspend fun genresMoods(): GenresMoods? {
        return client.browse(GENRES_MOODS_BROWSE_ID).toGenresMoods()
    }

    suspend fun genreMoodCategory(moodId: String): GenreMoodCategory? {
        return client.browse(GENRE_MOOD_CATEGORY_BROWSE_ID, moodId).toGenreMoodCategory()
    }


}