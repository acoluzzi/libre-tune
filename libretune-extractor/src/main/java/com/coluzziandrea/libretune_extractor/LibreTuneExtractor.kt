package com.coluzziandrea.libretune_extractor

import com.coluzziandrea.libretune_extractor.client.LibreClient
import com.coluzziandrea.libretune_extractor.model.ArtistDetails
import com.coluzziandrea.libretune_extractor.model.PlaylistDetails
import com.coluzziandrea.libretune_extractor.model.SearchResult
import com.coluzziandrea.libretune_extractor.model.SearchSuggestion
import com.coluzziandrea.libretune_extractor.parser.ArtistParser
import com.coluzziandrea.libretune_extractor.parser.PlaylistParser
import com.coluzziandrea.libretune_extractor.parser.SearchResultParser
import com.coluzziandrea.libretune_extractor.parser.toSearchSuggestions
import io.ktor.client.HttpClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LibreTuneExtractor @Inject constructor(
    httpClient: HttpClient
) {

    private val client = LibreClient(httpClient)


    suspend fun playlist(playlistId: String): PlaylistDetails? {
        return client.browse(playlistId).let {
            PlaylistParser.from(it)
        }
    }


    suspend fun artist(channelId: String): ArtistDetails? {
        return client.browse(channelId).let {
            ArtistParser.from(it, channelId)
        }
    }

    suspend fun search(query: String): SearchResult? {
        return client.search(URLEncoder.encode(query, StandardCharsets.UTF_8.name())).let {
            SearchResultParser.from(it)
        }
    }


    suspend fun searchSuggestions(query: String): List<SearchSuggestion> {
        return client.searchSuggestions(URLEncoder.encode(query, StandardCharsets.UTF_8.name()))
            .toSearchSuggestions()
    }


}