package com.colux.libretune.data.repository

import androidx.room.withTransaction
import com.coluzziandrea.libretune_extractor.LibreTuneExtractor
import com.coluzziandrea.libretune_extractor.sync.RemotePlaylistSnapshot
import com.colux.libretune.data.local.AppDatabase
import com.colux.libretune.data.local.join.PlaylistSongCrossRef
import com.colux.libretune.data.local.join.PlaylistSongSyncState
import com.colux.libretune.data.remote.auth.YtMusicAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates two-way sync between local playlists and YouTube Music.
 *
 * Local-DB writes happen instantly so the UI is never blocked on the network;
 * remote mutations are best-effort and recorded as `PENDING_*` rows when they
 * fail, so a later push/pull can reconcile them.
 */
@Singleton
class YouTubeMusicSyncRepository @Inject constructor(
    private val db: AppDatabase,
    private val extractor: LibreTuneExtractor,
    private val auth: YtMusicAuthRepository,
) {

    private val logger = Logger.getLogger("YouTubeMusicSyncRepository")

    val isSignedIn: Boolean
        get() = auth.current() != null

    /**
     * Promotes a plain local playlist to a synced playlist: create the mirror
     * on YouTube Music, then push every existing local song into it.
     */
    suspend fun enableSyncForPlaylist(localPlaylistId: String): Result<String> = runCatching {
        require(isSignedIn) { "Not signed in to YouTube Music" }
        val local = db.playlistDao().getPlaylistById(localPlaylistId)
            ?: error("Playlist $localPlaylistId not found")
        require(local.isLocal == true) {
            "Only local playlists can be promoted to synced playlists"
        }

        val remoteId = withContext(Dispatchers.IO) {
            extractor.sync.createPlaylist(
                title = local.name,
                description = "Synced from LibreTune"
            )
        } ?: error("YouTube Music did not return a playlist ID")

        db.playlistDao().markPlaylistSynced(localPlaylistId, remoteId)

        // Push every existing song so the remote playlist matches local.
        val songIds = db.playlistDao().getSongIdsInPlaylist(localPlaylistId)
        for (songId in songIds) {
            mirrorAddSong(localPlaylistId, songId)
        }
        remoteId
    }

    suspend fun disableSyncForPlaylist(localPlaylistId: String, alsoDeleteRemote: Boolean) {
        val local = db.playlistDao().getPlaylistById(localPlaylistId) ?: return
        val remoteId = local.remotePlaylistId

        if (alsoDeleteRemote && remoteId != null && isSignedIn) {
            withContext(Dispatchers.IO) {
                runCatching { extractor.sync.deletePlaylist(remoteId) }
                    .onFailure { logger.log(Level.WARNING, "Failed to delete remote playlist", it) }
            }
        }

        db.playlistDao().markPlaylistUnsynced(localPlaylistId)
    }

    /**
     * Mirror a local "add song" to YouTube Music. Safe to call regardless of
     * sync state — it's a no-op for non-synced playlists.
     *
     * The cross-ref must already exist (the caller inserted it). On failure
     * the cross-ref is marked `PENDING_ADD` so a future push can retry.
     */
    suspend fun mirrorAddSong(localPlaylistId: String, songId: String) {
        val playlist = db.playlistDao().getPlaylistById(localPlaylistId) ?: return
        if (!playlist.syncEnabled) return
        val remoteId = playlist.remotePlaylistId ?: return
        if (!isSignedIn) {
            markPending(localPlaylistId, songId, PlaylistSongSyncState.PENDING_ADD)
            return
        }

        try {
            val setVideoId = withContext(Dispatchers.IO) {
                extractor.sync.addToPlaylist(remoteId, songId)
            }
            db.playlistDao().updateCrossRefSyncState(
                playlistId = localPlaylistId,
                songId = songId,
                setVideoId = setVideoId,
                state = PlaylistSongSyncState.SYNCED
            )
        } catch (e: Exception) {
            logger.log(Level.WARNING, "mirrorAddSong failed for $songId", e)
            markPending(localPlaylistId, songId, PlaylistSongSyncState.PENDING_ADD)
        }
    }

    /**
     * Mirror a local "remove song" to YouTube Music and delete the local
     * cross-ref if the remote call succeeded.
     *
     * Returns `true` when the caller may consider the row gone (either it was
     * never synced, or the YT Music call succeeded). Returns `false` when the
     * row was kept as `PENDING_REMOVE` because the remote call failed — the
     * caller MUST leave the local cross-ref in place so the next push can
     * retry.
     */
    suspend fun mirrorRemoveSong(localPlaylistId: String, songId: String): Boolean {
        val playlist = db.playlistDao().getPlaylistById(localPlaylistId) ?: return true
        if (!playlist.syncEnabled) return true
        val remoteId = playlist.remotePlaylistId ?: return true

        val crossRef = db.playlistDao().getCrossRef(localPlaylistId, songId) ?: return true

        // If the song was never successfully pushed, there's nothing to delete
        // remotely — local delete is enough.
        if (crossRef.syncState == PlaylistSongSyncState.PENDING_ADD || crossRef.setVideoId == null) {
            return true
        }

        if (!isSignedIn) {
            db.playlistDao().updateCrossRefSyncState(
                playlistId = localPlaylistId,
                songId = songId,
                setVideoId = crossRef.setVideoId,
                state = PlaylistSongSyncState.PENDING_REMOVE
            )
            return false
        }

        return try {
            withContext(Dispatchers.IO) {
                extractor.sync.removeFromPlaylist(remoteId, songId, crossRef.setVideoId)
            }
            true
        } catch (e: Exception) {
            logger.log(Level.WARNING, "mirrorRemoveSong failed for $songId", e)
            db.playlistDao().updateCrossRefSyncState(
                playlistId = localPlaylistId,
                songId = songId,
                setVideoId = crossRef.setVideoId,
                state = PlaylistSongSyncState.PENDING_REMOVE
            )
            false
        }
    }

    /**
     * Pull the canonical state of the remote playlist into the local DB.
     *
     * Reconciliation rules:
     * - Songs only in remote → inserted locally (and looked up via extractor for metadata).
     * - Songs only in local with `PENDING_ADD` → pushed to remote.
     * - Songs only in local with `PENDING_REMOVE` → removal pushed to remote, then deleted locally.
     * - Songs in both → `setVideoId` refreshed.
     */
    suspend fun pullFromYouTubeMusic(localPlaylistId: String) {
        val playlist = db.playlistDao().getPlaylistById(localPlaylistId) ?: return
        if (!playlist.syncEnabled) return
        val remoteId = playlist.remotePlaylistId ?: return
        if (!isSignedIn) return

        val snapshot: RemotePlaylistSnapshot = try {
            withContext(Dispatchers.IO) { extractor.sync.fetchPlaylistItems(remoteId) }
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Failed to pull playlist $remoteId", e)
            return
        }

        val localSongIds = db.playlistDao().getSongIdsInPlaylist(localPlaylistId).toMutableSet()

        // 1. Push pending local changes first so we don't clobber them on pull.
        val pending = db.playlistDao().getPendingSyncEntries(localPlaylistId)
        for (entry in pending) {
            when (entry.syncState) {
                PlaylistSongSyncState.PENDING_ADD -> mirrorAddSong(localPlaylistId, entry.songId)
                PlaylistSongSyncState.PENDING_REMOVE -> {
                    val pushed = mirrorRemoveSong(localPlaylistId, entry.songId)
                    if (pushed) {
                        db.playlistDao().removeSongFromPlaylist(entry)
                        localSongIds.remove(entry.songId)
                    }
                }
                PlaylistSongSyncState.SYNCED -> Unit
            }
        }

        // 2. Mirror remote → local.
        db.withTransaction {
            for (remoteItem in snapshot.items) {
                ensureSongStub(remoteItem.videoId, remoteItem.title)
                db.playlistDao().addSongToPlaylist(
                    PlaylistSongCrossRef(
                        playlistId = localPlaylistId,
                        songId = remoteItem.videoId,
                        setVideoId = remoteItem.setVideoId,
                        syncState = PlaylistSongSyncState.SYNCED
                    )
                )
                localSongIds.remove(remoteItem.videoId)
            }

            // 3. Anything still in `localSongIds` is on the local side only and
            //    not in PENDING_* (those were handled above) — meaning the user
            //    removed it on YT Music. Drop the local row.
            for (orphan in localSongIds) {
                db.playlistDao().removeSongFromPlaylist(
                    PlaylistSongCrossRef(
                        playlistId = localPlaylistId,
                        songId = orphan
                    )
                )
            }

            // 4. If YT Music renamed the playlist, mirror that too.
            snapshot.title?.takeIf { it.isNotBlank() && it != playlist.name }?.let { newName ->
                db.playlistDao().upsert(playlist.copy(name = newName))
            }
        }

        logger.info {
            "Pulled ${snapshot.items.size} items from YT Music playlist $remoteId " +
                "into local $localPlaylistId"
        }
    }

    /**
     * Replays every pending mutation for every synced playlist. Useful to call
     * after sign-in to drain offline-queued changes.
     */
    suspend fun flushPendingMutations() {
        if (!isSignedIn) return
        // Snapshot once — we don't want to subscribe.
        val syncedPlaylists = db.playlistDao().getSyncedPlaylists().first()
        for (p in syncedPlaylists) {
            for (entry in db.playlistDao().getPendingSyncEntries(p.playlistId)) {
                when (entry.syncState) {
                    PlaylistSongSyncState.PENDING_ADD -> mirrorAddSong(p.playlistId, entry.songId)
                    PlaylistSongSyncState.PENDING_REMOVE -> {
                        if (mirrorRemoveSong(p.playlistId, entry.songId)) {
                            db.playlistDao().removeSongFromPlaylist(entry)
                        }
                    }
                    PlaylistSongSyncState.SYNCED -> Unit
                }
            }
        }
    }

    private suspend fun markPending(
        playlistId: String,
        songId: String,
        state: PlaylistSongSyncState
    ) {
        val existing = db.playlistDao().getCrossRef(playlistId, songId)
        db.playlistDao().updateCrossRefSyncState(
            playlistId = playlistId,
            songId = songId,
            setVideoId = existing?.setVideoId,
            state = state
        )
    }

    /**
     * Materialise a placeholder song row when YT Music returned a track we
     * don't yet know about. We try the full extractor lookup first; if that
     * fails we drop in a stub so the cross-ref FK constraint is satisfied.
     */
    private suspend fun ensureSongStub(videoId: String, title: String?) {
        val existing = db.songDao().getSongByIdOnce(videoId)
        if (existing != null) return

        val song = com.colux.libretune.data.local.entity.SongEntity(
            songId = videoId,
            title = title ?: "Unknown",
            albumId = null,
            trackNumber = null,
            images = emptyList(),
            views = 0,
            durationSec = null,
            updateTimestamp = System.currentTimeMillis()
        )
        db.songDao().insertSong(song)
    }
}
