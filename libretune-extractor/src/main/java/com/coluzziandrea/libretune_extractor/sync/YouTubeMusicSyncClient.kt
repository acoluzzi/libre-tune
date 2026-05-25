package com.coluzziandrea.libretune_extractor.sync

import com.coluzziandrea.libretune_extractor.client.LibreClient
import com.coluzziandrea.libretune_extractor.client.request.EditPlaylistAction
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Authenticated playlist mutations against YouTube Music.
 *
 * Every method here assumes the [LibreClient] was constructed with a real
 * [com.coluzziandrea.libretune_extractor.auth.AuthProvider]; the client will
 * throw `IllegalStateException` if the user isn't signed in.
 */
class YouTubeMusicSyncClient(
    private val client: LibreClient
) {

    /**
     * Creates a new playlist owned by the signed-in user. Returns the YT Music
     * playlist ID (something like `PLxxxxxxx`).
     */
    suspend fun createPlaylist(
        title: String,
        description: String = "",
        privacyStatus: String = "PRIVATE",
        seedVideoIds: List<String> = emptyList()
    ): String? {
        val resp = client.createPlaylist(title, description, privacyStatus, seedVideoIds)
        return resp.playlistId
    }

    suspend fun deletePlaylist(playlistId: String) {
        client.deletePlaylist(playlistId)
    }

    /**
     * Adds one video to a playlist and returns the per-occurrence `setVideoId`
     * that the caller must persist to be able to remove it later.
     */
    suspend fun addToPlaylist(playlistId: String, videoId: String): String? {
        val resp = client.editPlaylist(
            playlistId = playlistId,
            actions = listOf(EditPlaylistAction.add(videoId))
        )
        return resp.playlistEditResults
            .firstNotNullOfOrNull { it.playlistEditVideoAddedResultData?.setVideoId }
    }

    suspend fun removeFromPlaylist(
        playlistId: String,
        videoId: String,
        setVideoId: String
    ) {
        client.editPlaylist(
            playlistId = playlistId,
            actions = listOf(EditPlaylistAction.remove(videoId, setVideoId))
        )
    }

    /**
     * Pulls every item of a YT Music playlist, including the `setVideoId`s. Use
     * `PL…` (not `VL…`) as the playlist ID — the client adds the `VL` prefix
     * for the browse call internally.
     */
    suspend fun fetchPlaylistItems(playlistId: String): RemotePlaylistSnapshot {
        val browseId = if (playlistId.startsWith("VL")) playlistId else "VL$playlistId"
        val raw = client.browseRaw(browseId)
        val items = mutableListOf<SyncedPlaylistItem>()
        collectPlaylistItems(raw, items)
        val title = findFirstTitleString(raw)
        return RemotePlaylistSnapshot(
            playlistId = playlistId.removePrefix("VL"),
            title = title,
            items = items
        )
    }

    /**
     * Lists every playlist visible in the signed-in user's library
     * ("Your Library → Playlists" on the YT Music web UI).
     */
    suspend fun fetchLibraryPlaylists(): List<LibraryPlaylistSummary> {
        val raw = client.browseRaw("FEmusic_liked_playlists")
        val out = mutableListOf<LibraryPlaylistSummary>()
        collectLibraryPlaylists(raw, out)
        return out
    }

    private fun collectPlaylistItems(
        node: JsonElement,
        out: MutableList<SyncedPlaylistItem>
    ) {
        when (node) {
            is JsonObject -> {
                val renderer = node["musicResponsiveListItemRenderer"]
                if (renderer is JsonObject) {
                    parseMusicResponsiveListItem(renderer)?.let(out::add)
                }
                node.values.forEach { collectPlaylistItems(it, out) }
            }

            is JsonArray -> node.forEach { collectPlaylistItems(it, out) }
            else -> Unit
        }
    }

    private fun parseMusicResponsiveListItem(renderer: JsonObject): SyncedPlaylistItem? {
        val itemData = renderer["playlistItemData"] as? JsonObject ?: return null
        val videoId = itemData["videoId"]?.jsonPrimitive?.contentOrNull ?: return null
        val setVideoId = itemData["playlistSetVideoId"]?.jsonPrimitive?.contentOrNull

        val flexColumns = renderer["flexColumns"] as? JsonArray
        val titles = flexColumns?.mapNotNull { firstRunText(it) }.orEmpty()

        return SyncedPlaylistItem(
            videoId = videoId,
            setVideoId = setVideoId,
            title = titles.getOrNull(0),
            artistNames = titles.getOrNull(1),
            albumName = titles.getOrNull(2)
        )
    }

    private fun firstRunText(column: JsonElement): String? {
        if (column !is JsonObject) return null
        val renderer = column["musicResponsiveListItemFlexColumnRenderer"] as? JsonObject
            ?: return null
        val text = renderer["text"] as? JsonObject ?: return null
        val runs = text["runs"] as? JsonArray ?: return null
        return runs.firstNotNullOfOrNull {
            (it as? JsonObject)?.get("text")?.jsonPrimitive?.contentOrNull
        }
    }

    private fun collectLibraryPlaylists(
        node: JsonElement,
        out: MutableList<LibraryPlaylistSummary>
    ) {
        when (node) {
            is JsonObject -> {
                val twoRow = node["musicTwoRowItemRenderer"] as? JsonObject
                if (twoRow != null) {
                    parseTwoRowPlaylistEntry(twoRow)?.let(out::add)
                }
                node.values.forEach { collectLibraryPlaylists(it, out) }
            }

            is JsonArray -> node.forEach { collectLibraryPlaylists(it, out) }
            else -> Unit
        }
    }

    private fun parseTwoRowPlaylistEntry(renderer: JsonObject): LibraryPlaylistSummary? {
        val nav = renderer["navigationEndpoint"] as? JsonObject ?: return null
        val browse = nav["browseEndpoint"] as? JsonObject ?: return null
        val browseId = browse["browseId"]?.jsonPrimitive?.contentOrNull ?: return null
        // YT Music prefixes playlist browse IDs with "VL".
        if (!browseId.startsWith("VL")) return null

        val title = (renderer["title"] as? JsonObject)
            ?.let { (it["runs"] as? JsonArray)?.firstOrNull() }
            ?.let { (it as? JsonObject)?.get("text")?.jsonPrimitive?.contentOrNull }
            ?: return null

        return LibraryPlaylistSummary(
            playlistId = browseId.removePrefix("VL"),
            title = title
        )
    }

    private fun findFirstTitleString(node: JsonElement): String? {
        if (node is JsonObject) {
            val header = node["musicDetailHeaderRenderer"] as? JsonObject
                ?: node["musicEditablePlaylistDetailHeaderRenderer"] as? JsonObject
                ?: node["musicResponsiveHeaderRenderer"] as? JsonObject
            if (header != null) {
                val title = (header["title"] as? JsonObject)
                    ?.let { (it["runs"] as? JsonArray)?.firstOrNull() }
                    ?.let { (it as? JsonObject)?.get("text")?.jsonPrimitive?.contentOrNull }
                if (!title.isNullOrBlank()) return title
            }
            for (v in node.values) {
                val t = findFirstTitleString(v)
                if (!t.isNullOrBlank()) return t
            }
        } else if (node is JsonArray) {
            for (v in node) {
                val t = findFirstTitleString(v)
                if (!t.isNullOrBlank()) return t
            }
        }
        return null
    }
}
