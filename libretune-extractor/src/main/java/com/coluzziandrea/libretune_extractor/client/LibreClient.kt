package com.coluzziandrea.libretune_extractor.client

import com.coluzziandrea.libretune_extractor.client.request.BrowseRequest
import com.coluzziandrea.libretune_extractor.client.request.Client
import com.coluzziandrea.libretune_extractor.client.request.Context
import com.coluzziandrea.libretune_extractor.client.request.SearchRequest
import com.coluzziandrea.libretune_extractor.client.request.SearchSuggestionRequest
import com.coluzziandrea.libretune_extractor.client.response.BrowseData
import com.coluzziandrea.libretune_extractor.client.response.MultipleContentBrowseData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class LibreClient(
    private val client: HttpClient
) {

    val BASE_URL = "https://music.youtube.com/youtubei/v1"

    private fun getContext(): Context {
        return Context(
            client = Client(
                clientName = "WEB_REMIX",
                clientVersion = "1.20250827.05.00"
            )
        )
    }

    suspend fun search(query: String): BrowseData {
        val res = client.post("$BASE_URL/search?prettyPrint=false") {
            contentType(ContentType.Application.Json)

            setHeaders()

            setBody(
                SearchRequest(
                    query = query,
                    context = getContext()
                )
            )
        }.body<BrowseData>()
        return res
    }

    suspend fun searchSuggestions(input: String): MultipleContentBrowseData {
        val res = client.post("$BASE_URL/music/get_search_suggestions?prettyPrint=false") {
            contentType(ContentType.Application.Json)

            setHeaders()

            setBody(
                SearchSuggestionRequest(
                    input = input,
                    context = getContext()
                )
            )
        }.body<MultipleContentBrowseData>()
        return res
    }

    suspend fun browse(browseId: String): BrowseData {
        val res = client.post("$BASE_URL/browse?prettyPrint=false") {
            contentType(ContentType.Application.Json)

            setHeaders()

            setBody(
                BrowseRequest(
                    browseId = browseId,
                    context = getContext()
                )
            )
        }.body<BrowseData>()
        return res
    }

    private fun HttpRequestBuilder.setHeaders() {
        header(
            "User-Agent",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36"
        )
        header("Accept-Language", "en-US,en;q=0.9")
        header(
            "Accept",
            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8"
        )
    }
}