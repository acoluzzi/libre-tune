package com.coluzziandrea.libretune_extractor.client

import com.coluzziandrea.libretune_extractor.auth.AuthProvider
import com.coluzziandrea.libretune_extractor.auth.SapisidHash
import com.coluzziandrea.libretune_extractor.auth.YtmAuthState
import com.coluzziandrea.libretune_extractor.client.request.BrowseRequest
import com.coluzziandrea.libretune_extractor.client.request.Client
import com.coluzziandrea.libretune_extractor.client.request.Context
import com.coluzziandrea.libretune_extractor.client.request.CreatePlaylistRequest
import com.coluzziandrea.libretune_extractor.client.request.DeletePlaylistRequest
import com.coluzziandrea.libretune_extractor.client.request.EditPlaylistAction
import com.coluzziandrea.libretune_extractor.client.request.EditPlaylistRequest
import com.coluzziandrea.libretune_extractor.client.request.SearchRequest
import com.coluzziandrea.libretune_extractor.client.request.SearchSuggestionRequest
import com.coluzziandrea.libretune_extractor.client.response.BrowseData
import com.coluzziandrea.libretune_extractor.client.response.CreatePlaylistResponse
import com.coluzziandrea.libretune_extractor.client.response.DeletePlaylistResponse
import com.coluzziandrea.libretune_extractor.client.response.EditPlaylistResponse
import com.coluzziandrea.libretune_extractor.client.response.MultipleContentBrowseData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonElement

class LibreClient(
    private val client: HttpClient,
    private val authProvider: AuthProvider = AuthProvider.Anonymous
) {

    companion object {
        const val BASE_URL = "https://music.youtube.com/youtubei/v1"

        const val CLIENT_NAME = "WEB_REMIX"
        const val CLIENT_VERSION = "1.20250827.05.00"
        const val CLIENT_ID_HEADER_VALUE = "67"
    }


    private fun getContext(): Context {
        return Context(
            client = Client(
                clientName = CLIENT_NAME,
                clientVersion = CLIENT_VERSION
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

    suspend fun browse(browseId: String, params: String? = null): BrowseData {
        val res = client.post("$BASE_URL/browse?prettyPrint=false") {
            contentType(ContentType.Application.Json)

            setHeaders()

            setBody(
                BrowseRequest(
                    browseId = browseId,
                    context = getContext(),
                    params
                )
            )
        }.body<BrowseData>()
        return res
    }

    /**
     * Returns the raw JSON tree for a browse call — used by the sync layer to
     * pull `playlistItemData.playlistSetVideoId` values that the typed response
     * classes don't model.
     */
    suspend fun browseRaw(browseId: String, params: String? = null): JsonElement {
        return client.post("$BASE_URL/browse?prettyPrint=false") {
            contentType(ContentType.Application.Json)
            setHeaders()
            setBody(
                BrowseRequest(
                    browseId = browseId,
                    context = getContext(),
                    params
                )
            )
        }.body<JsonElement>()
    }

    suspend fun editPlaylist(
        playlistId: String,
        actions: List<EditPlaylistAction>
    ): EditPlaylistResponse {
        requireAuthenticated("editPlaylist")
        return client.post("$BASE_URL/browse/edit_playlist?prettyPrint=false") {
            contentType(ContentType.Application.Json)
            setHeaders()
            setBody(
                EditPlaylistRequest(
                    playlistId = playlistId,
                    actions = actions,
                    context = getContext()
                )
            )
        }.body<EditPlaylistResponse>()
    }

    suspend fun createPlaylist(
        title: String,
        description: String = "",
        privacyStatus: String = "PRIVATE",
        videoIds: List<String> = emptyList()
    ): CreatePlaylistResponse {
        requireAuthenticated("createPlaylist")
        return client.post("$BASE_URL/playlist/create?prettyPrint=false") {
            contentType(ContentType.Application.Json)
            setHeaders()
            setBody(
                CreatePlaylistRequest(
                    title = title,
                    description = description,
                    privacyStatus = privacyStatus,
                    videoIds = videoIds,
                    context = getContext()
                )
            )
        }.body<CreatePlaylistResponse>()
    }

    suspend fun deletePlaylist(playlistId: String): DeletePlaylistResponse {
        requireAuthenticated("deletePlaylist")
        return client.post("$BASE_URL/playlist/delete?prettyPrint=false") {
            contentType(ContentType.Application.Json)
            setHeaders()
            setBody(
                DeletePlaylistRequest(
                    playlistId = playlistId,
                    context = getContext()
                )
            )
        }.body<DeletePlaylistResponse>()
    }

    private fun requireAuthenticated(operation: String) {
        if (authProvider.current() == null) {
            throw IllegalStateException(
                "$operation requires the user to be signed in to YouTube Music"
            )
        }
    }

    private fun HttpRequestBuilder.setHeaders() {
        val auth = authProvider.current()
        header("User-Agent", auth?.userAgent ?: YtmAuthState.DEFAULT_USER_AGENT)
        header("Accept-Language", "en-US,en;q=0.9")
        header(
            "Accept",
            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8"
        )
        header("X-YouTube-Client-Name", CLIENT_ID_HEADER_VALUE)
        header("X-YouTube-Client-Version", CLIENT_VERSION)

        if (auth != null) {
            header("Cookie", auth.cookieHeader)
            header("Origin", auth.origin)
            header("X-Origin", auth.origin)
            header("X-Goog-AuthUser", "0")
            header("Authorization", SapisidHash.authorizationHeader(auth.sapisid, auth.origin))
            auth.visitorData?.let { header("X-Goog-Visitor-Id", it) }
        }
    }
}
