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
import io.ktor.http.encodeURLParameter

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

    suspend fun search(query: String): SearchResult? {
        return client.search(query.encodeURLParameter()).let {
            SearchResultParser.from(it)
        }
    }


    suspend fun searchSuggestions(query: String): List<SearchSuggestion> {
        return client.searchSuggestions(query.encodeURLParameter())
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