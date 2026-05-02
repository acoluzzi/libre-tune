package com.colux.libretune.data.repository

import com.colux.libretune.data.remote.backend.BackendApi
import com.colux.libretune.data.remote.backend.BackendTokenStore
import com.colux.libretune.data.remote.backend.LikedSongsPayload
import com.colux.libretune.data.remote.backend.PlaylistsPayload
import com.colux.libretune.data.remote.backend.SavedAlbumsPayload
import com.colux.libretune.data.remote.backend.SavedArtistsPayload
import com.colux.libretune.data.remote.backend.SyncStateResponse
import com.colux.libretune.data.sync.SyncMetadataStore

/**
 * High-level operations against the LibreTune backend: authenticate the
 * user and push / pull the four library collections we keep in sync.
 */
class BackendSyncRepository(
    private val api: BackendApi,
    private val tokenStore: BackendTokenStore,
    private val syncMetadata: SyncMetadataStore,
) {
    fun isAuthenticated(): Boolean = tokenStore.isAuthenticated()

    suspend fun register(username: String, password: String, email: String? = null) =
        api.register(username, password, email)

    suspend fun login(username: String, password: String) = api.login(username, password)

    suspend fun logout() {
        api.logout()
        syncMetadata.clear()
    }

    suspend fun fetchState(): SyncStateResponse = api.fetchState()

    suspend fun pullLikedSongs(): LikedSongsPayload = api.fetchLikedSongs()
    suspend fun pushLikedSongs(payload: LikedSongsPayload) = api.pushLikedSongs(payload)

    suspend fun pullPlaylists(): PlaylistsPayload = api.fetchPlaylists()
    suspend fun pushPlaylists(payload: PlaylistsPayload) = api.pushPlaylists(payload)

    suspend fun pullSavedAlbums(): SavedAlbumsPayload = api.fetchSavedAlbums()
    suspend fun pushSavedAlbums(payload: SavedAlbumsPayload) = api.pushSavedAlbums(payload)

    suspend fun pullSavedArtists(): SavedArtistsPayload = api.fetchSavedArtists()
    suspend fun pushSavedArtists(payload: SavedArtistsPayload) = api.pushSavedArtists(payload)
}
