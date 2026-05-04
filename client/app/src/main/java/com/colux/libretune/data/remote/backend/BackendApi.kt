package com.colux.libretune.data.remote.backend

import com.colux.libretune.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.serialization.Serializable

/**
 * Client for the LibreTune backend service hosted at
 * [BuildConfig.BACKEND_BASE_URL] (https://libretune.coluzziandrea.com).
 *
 * Handles user authentication and library sync (liked songs, playlists,
 * saved albums, saved artists). Pass the token returned by [register] /
 * [login] to every sync call.
 */
@Singleton
class BackendApi @Inject constructor(
    private val httpClient: HttpClient,
    private val tokenStore: BackendTokenStore,
) {
    private val baseUrl: String = BuildConfig.BACKEND_BASE_URL.trimEnd('/')

    suspend fun register(username: String, password: String, email: String? = null): AuthResponse {
        val response: AuthResponse = httpClient.post("$baseUrl/api/auth/register/") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(username = username, password = password, email = email.orEmpty()))
        }.body()
        tokenStore.save(response.token)
        return response
    }

    suspend fun login(username: String, password: String): AuthResponse {
        val response: AuthResponse = httpClient.post("$baseUrl/api/auth/login/") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username = username, password = password))
        }.body()
        tokenStore.save(response.token)
        return response
    }

    suspend fun logout() {
        val token = tokenStore.get() ?: return
        httpClient.post("$baseUrl/api/auth/logout/") {
            header(HttpHeaders.Authorization, "Token $token")
        }
        tokenStore.clear()
    }

    suspend fun me(): RemoteUser =
        authedGet("$baseUrl/api/auth/me/")

    suspend fun fetchState(): SyncStateResponse =
        authedGet("$baseUrl/api/sync/state/")

    suspend fun fetchLikedSongs(): LikedSongsPayload =
        authedGet("$baseUrl/api/sync/liked-songs/")

    suspend fun pushLikedSongs(payload: LikedSongsPayload) {
        authedPut("$baseUrl/api/sync/liked-songs/", payload)
    }

    suspend fun fetchPlaylists(): PlaylistsPayload =
        authedGet("$baseUrl/api/sync/playlists/")

    suspend fun pushPlaylists(payload: PlaylistsPayload) {
        authedPut("$baseUrl/api/sync/playlists/", payload)
    }

    suspend fun fetchSavedAlbums(): SavedAlbumsPayload =
        authedGet("$baseUrl/api/sync/saved-albums/")

    suspend fun pushSavedAlbums(payload: SavedAlbumsPayload) {
        authedPut("$baseUrl/api/sync/saved-albums/", payload)
    }

    suspend fun fetchSavedArtists(): SavedArtistsPayload =
        authedGet("$baseUrl/api/sync/saved-artists/")

    suspend fun pushSavedArtists(payload: SavedArtistsPayload) {
        authedPut("$baseUrl/api/sync/saved-artists/", payload)
    }

    private suspend inline fun <reified T> authedGet(url: String): T =
        httpClient.get(url) {
            header(HttpHeaders.Authorization, "Token ${requireToken()}")
        }.body()

    private suspend inline fun <reified T> authedPut(url: String, body: T) {
        httpClient.put(url) {
            header(HttpHeaders.Authorization, "Token ${requireToken()}")
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    private fun requireToken(): String =
        tokenStore.get() ?: error("Not authenticated. Call register() or login() first.")
}

@Serializable
data class RegisterRequest(val username: String, val password: String, val email: String = "")

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class AuthResponse(val token: String, val user: RemoteUser)

@Serializable
data class RemoteUser(val id: Long, val username: String, val email: String = "")

@Serializable
data class RemoteSong(
    val remote_id: String,
    val title: String,
    val artist_name: String = "",
    val album_name: String = "",
    val duration_ms: Long = 0,
    val thumbnail_url: String = "",
)

@Serializable
data class LikedSongItem(val song: RemoteSong, val position: Int = 0)

@Serializable
data class LikedSongsPayload(
    val last_updated_ms: Long? = null,
    val items: List<LikedSongItem>,
)

@Serializable
data class PlaylistEntry(val song: RemoteSong, val position: Int = 0)

@Serializable
data class RemotePlaylist(
    val remote_id: String = "",
    val name: String,
    val description: String = "",
    val thumbnail_url: String = "",
    val songs: List<PlaylistEntry> = emptyList(),
)

@Serializable
data class PlaylistsPayload(
    val last_updated_ms: Long? = null,
    val items: List<RemotePlaylist>,
)

@Serializable
data class RemoteAlbum(
    val remote_id: String,
    val name: String,
    val artist_name: String = "",
    val thumbnail_url: String = "",
    val position: Int = 0,
)

@Serializable
data class SavedAlbumsPayload(
    val last_updated_ms: Long? = null,
    val items: List<RemoteAlbum>,
)

@Serializable
data class RemoteArtist(
    val remote_id: String,
    val name: String,
    val thumbnail_url: String = "",
    val position: Int = 0,
)

@Serializable
data class SavedArtistsPayload(
    val last_updated_ms: Long? = null,
    val items: List<RemoteArtist>,
)

@Serializable
data class SyncStateResponse(
    val liked_songs: Long? = null,
    val playlists: Long? = null,
    val saved_albums: Long? = null,
    val saved_artists: Long? = null,
)
