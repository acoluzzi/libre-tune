package com.colux.libretune.data.sync

import com.colux.libretune.data.local.dbWithTransaction
import com.colux.libretune.data.local.AppDatabase
import com.colux.libretune.data.local.DatabaseConstants
import com.colux.libretune.data.local.entity.AlbumType
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.ImageAttribute
import com.colux.libretune.data.local.entity.LibraryEntity
import com.colux.libretune.data.local.entity.LibraryItemType
import com.colux.libretune.data.local.entity.PlaylistEntity
import com.colux.libretune.data.local.entity.SongEntity
import com.colux.libretune.data.local.join.PlaylistSongCrossRef
import com.colux.libretune.data.remote.backend.LikedSongItem
import com.colux.libretune.data.remote.backend.LikedSongsPayload
import com.colux.libretune.data.remote.backend.PlaylistEntry
import com.colux.libretune.data.remote.backend.PlaylistsPayload
import com.colux.libretune.data.remote.backend.RemotePlaylist
import com.colux.libretune.data.remote.backend.SavedAlbumsPayload
import com.colux.libretune.data.remote.backend.SavedArtistsPayload
import com.colux.libretune.data.remote.backend.SyncStateResponse
import com.colux.libretune.data.repository.BackendSyncRepository
import kotlinx.coroutines.flow.first
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Owns the local⇄remote synchronisation policy.
 *
 * For every [SyncCollection] the orchestrator compares three values on each
 * batch run:
 *
 * - `remote_ts` — the server's `last_updated_ms` for this collection
 *   (`null` if the server has never received data for it).
 * - `local_ts` — `localChangedAt` from [SyncMetadataStore], i.e. the most
 *   recent local mutation.
 * - `seen_remote_ts` — `remoteUpdatedAt`, the last server timestamp this
 *   client mirrored.
 *
 * The decision is last-writer-wins:
 *
 * - `remote_ts == null` and local is non-empty → push (and stamp the
 *   server with `local_ts`).
 * - `remote_ts > local_ts` → pull (the server has a fresher snapshot).
 * - `remote_ts < local_ts` → push (the client has a fresher snapshot).
 * - otherwise → no-op.
 *
 * Local data is the source of truth on conflict, but a remote snapshot
 * with a strictly higher timestamp wins because it represents a write
 * the user made on another device after this device's last edit.
 */
class LibrarySyncOrchestrator(
    private val backend: BackendSyncRepository,
    private val db: AppDatabase,
    private val metadata: SyncMetadataStore,
) {
    private val logger = Logger.getLogger("LibrarySyncOrchestrator")

    suspend fun syncAll(): Result<Unit> = runCatching {
        if (!backend.isAuthenticated()) {
            logger.info("Not authenticated; skipping sync.")
            return@runCatching
        }
        val state = backend.fetchState()
        SyncCollection.values().forEach { collection ->
            try {
                syncCollection(collection, state.remoteTimestamp(collection))
            } catch (t: Throwable) {
                logger.log(Level.WARNING, "Sync failed for $collection", t)
            }
        }
    }

    suspend fun syncCollection(collection: SyncCollection, remoteTs: Long?) {
        if (!backend.isAuthenticated()) return
        val localTs = metadata.localChangedAt(collection)

        when {
            remoteTs == null -> {
                if (!isLocalEmpty(collection)) {
                    logger.info("Pushing $collection (server has nothing).")
                    val effectiveTs = ensureLocalTimestamp(collection, localTs)
                    push(collection, effectiveTs)
                    metadata.setRemoteUpdatedAt(collection, effectiveTs)
                } else {
                    logger.info("Skipping $collection (both sides empty).")
                }
            }
            remoteTs > localTs -> {
                logger.info("Pulling $collection (remote $remoteTs > local $localTs).")
                pull(collection)
                metadata.setLocalChangedAt(collection, remoteTs)
                metadata.setRemoteUpdatedAt(collection, remoteTs)
            }
            remoteTs < localTs -> {
                logger.info("Pushing $collection (local $localTs > remote $remoteTs).")
                push(collection, localTs)
                metadata.setRemoteUpdatedAt(collection, localTs)
            }
            else -> logger.info("$collection already in sync at $remoteTs.")
        }
    }

    /** First-time push for a user with pre-existing local data: invent a
     *  timestamp so the comparison rules above remain monotonic. */
    private fun ensureLocalTimestamp(collection: SyncCollection, localTs: Long): Long =
        if (localTs > 0) localTs else System.currentTimeMillis().also {
            metadata.setLocalChangedAt(collection, it)
        }

    // ---------------------------------------------------------------------
    // PUSH
    // ---------------------------------------------------------------------

    private suspend fun push(collection: SyncCollection, timestamp: Long) {
        when (collection) {
            SyncCollection.LIKED_SONGS ->
                backend.pushLikedSongs(buildLikedSongsPayload(timestamp))
            SyncCollection.PLAYLISTS ->
                backend.pushPlaylists(buildPlaylistsPayload(timestamp))
            SyncCollection.SAVED_ALBUMS ->
                backend.pushSavedAlbums(buildSavedAlbumsPayload(timestamp))
            SyncCollection.SAVED_ARTISTS ->
                backend.pushSavedArtists(buildSavedArtistsPayload(timestamp))
        }
    }

    private suspend fun buildLikedSongsPayload(timestamp: Long): LikedSongsPayload {
        val songs = db.songDao()
            .getSongsWithAlbumAndArtistByPlaylistId(DatabaseConstants.LIKED_SONGS_PLAYLIST_ID)
            .first()
        val items = songs.mapIndexed { index, swaa ->
            LikedSongItem(
                song = swaa.song.toRemote(
                    artistName = swaa.artists.joinToString(", ") { it.name },
                    albumName = swaa.album?.name.orEmpty(),
                ),
                position = index,
            )
        }
        return LikedSongsPayload(last_updated_ms = timestamp, items = items)
    }

    private suspend fun buildPlaylistsPayload(timestamp: Long): PlaylistsPayload {
        val playlists = db.playlistDao().getSavedPlaylists().first()
            .filter {
                it.playlist.type == AlbumType.PLAYLIST &&
                    it.playlist.playlistId != DatabaseConstants.LIKED_SONGS_PLAYLIST_ID
            }
        val items = playlists.map { pws ->
            val songEntries = db.songDao()
                .getSongsWithAlbumAndArtistByPlaylistId(pws.playlist.playlistId)
                .first()
                .mapIndexed { index, swaa ->
                    PlaylistEntry(
                        song = swaa.song.toRemote(
                            artistName = swaa.artists.joinToString(", ") { it.name },
                            albumName = swaa.album?.name.orEmpty(),
                        ),
                        position = index,
                    )
                }
            RemotePlaylist(
                remote_id = if (pws.playlist.isLocal == true) "" else pws.playlist.playlistId,
                name = pws.playlist.name,
                description = "",
                thumbnail_url = pws.playlist.images.bestUrl(),
                songs = songEntries,
            )
        }
        return PlaylistsPayload(last_updated_ms = timestamp, items = items)
    }

    private suspend fun buildSavedAlbumsPayload(timestamp: Long): SavedAlbumsPayload {
        val albums = db.playlistDao().getSavedPlaylists().first()
            .filter { it.playlist.type != AlbumType.PLAYLIST }
        val items = albums.mapIndexed { index, pws ->
            pws.playlist.toRemoteAlbum(
                artistName = pws.artists.joinToString(", ") { it.name },
                position = index,
            )
        }
        return SavedAlbumsPayload(last_updated_ms = timestamp, items = items)
    }

    private suspend fun buildSavedArtistsPayload(timestamp: Long): SavedArtistsPayload {
        val artists = db.artistDao().getSavedArtists().first()
        val items = artists.mapIndexed { index, artist ->
            artist.toRemoteArtist(position = index)
        }
        return SavedArtistsPayload(last_updated_ms = timestamp, items = items)
    }

    // ---------------------------------------------------------------------
    // PULL
    // ---------------------------------------------------------------------

    private suspend fun pull(collection: SyncCollection) {
        when (collection) {
            SyncCollection.LIKED_SONGS -> applyLikedSongs(backend.pullLikedSongs())
            SyncCollection.PLAYLISTS -> applyPlaylists(backend.pullPlaylists())
            SyncCollection.SAVED_ALBUMS -> applySavedAlbums(backend.pullSavedAlbums())
            SyncCollection.SAVED_ARTISTS -> applySavedArtists(backend.pullSavedArtists())
        }
    }

    private suspend fun applyLikedSongs(payload: LikedSongsPayload) {
        val now = System.currentTimeMillis()
        db.dbWithTransaction {
            db.playlistDao().clearLikedSongs()
            payload.items.forEach { item ->
                db.songDao().insertSong(item.song.toEntityStub(now))
                db.playlistDao().addSongToPlaylist(
                    PlaylistSongCrossRef(
                        playlistId = DatabaseConstants.LIKED_SONGS_PLAYLIST_ID,
                        songId = item.song.remote_id,
                    )
                )
            }
        }
    }

    private suspend fun applyPlaylists(payload: PlaylistsPayload) {
        val now = System.currentTimeMillis()
        db.dbWithTransaction {
            db.playlistDao().clearSyncedPlaylists()
            payload.items.forEach { remotePlaylist ->
                val playlistId = remotePlaylist.remote_id.ifEmpty {
                    "remote-${now}-${remotePlaylist.name.hashCode()}"
                }
                val isLocal = remotePlaylist.remote_id.isEmpty()
                db.playlistDao().insert(
                    PlaylistEntity(
                        playlistId = playlistId,
                        name = remotePlaylist.name,
                        images = listOfNotNull(remotePlaylist.thumbnail_url.toImageAttribute()),
                        type = AlbumType.PLAYLIST,
                        isLocal = isLocal,
                        updateTimestamp = now,
                    )
                )
                if (!isLocal) {
                    db.libraryDao().insert(
                        LibraryEntity(
                            id = playlistId,
                            type = LibraryItemType.PLAYLIST,
                            playlistId = playlistId,
                            addedAtTimestamp = now,
                        )
                    )
                }
                remotePlaylist.songs.forEach { entry ->
                    db.songDao().insertSong(entry.song.toEntityStub(now))
                    db.playlistDao().addSongToPlaylist(
                        PlaylistSongCrossRef(playlistId = playlistId, songId = entry.song.remote_id)
                    )
                }
            }
        }
    }

    private suspend fun applySavedAlbums(payload: SavedAlbumsPayload) {
        val now = System.currentTimeMillis()
        db.dbWithTransaction {
            db.libraryDao().clearSavedAlbumLinks()
            payload.items.forEach { album ->
                db.playlistDao().insert(
                    PlaylistEntity(
                        playlistId = album.remote_id,
                        name = album.name,
                        images = listOfNotNull(album.thumbnail_url.toImageAttribute()),
                        type = AlbumType.ALBUM,
                        isLocal = false,
                        updateTimestamp = now,
                    )
                )
                db.libraryDao().insert(
                    LibraryEntity(
                        id = album.remote_id,
                        type = LibraryItemType.PLAYLIST,
                        playlistId = album.remote_id,
                        addedAtTimestamp = now,
                    )
                )
            }
        }
    }

    private suspend fun applySavedArtists(payload: SavedArtistsPayload) {
        val now = System.currentTimeMillis()
        db.dbWithTransaction {
            db.libraryDao().clearSavedArtistLinks()
            payload.items.forEach { artist ->
                db.artistDao().upsert(
                    ArtistEntity(
                        artistId = artist.remote_id,
                        name = artist.name,
                        images = listOfNotNull(artist.thumbnail_url.toImageAttribute()),
                        updateTimestamp = now,
                    )
                )
                db.libraryDao().insert(
                    LibraryEntity(
                        id = artist.remote_id,
                        type = LibraryItemType.ARTIST,
                        artistId = artist.remote_id,
                        addedAtTimestamp = now,
                    )
                )
            }
        }
    }

    // ---------------------------------------------------------------------
    // EMPTY CHECKS
    // ---------------------------------------------------------------------

    private suspend fun isLocalEmpty(collection: SyncCollection): Boolean = when (collection) {
        SyncCollection.LIKED_SONGS ->
            db.songDao().getSongsInPlaylist(DatabaseConstants.LIKED_SONGS_PLAYLIST_ID).isEmpty()
        SyncCollection.PLAYLISTS -> db.playlistDao().getSavedPlaylists().first()
            .none {
                it.playlist.type == AlbumType.PLAYLIST &&
                    it.playlist.playlistId != DatabaseConstants.LIKED_SONGS_PLAYLIST_ID
            }
        SyncCollection.SAVED_ALBUMS -> db.playlistDao().getSavedPlaylists().first()
            .none { it.playlist.type != AlbumType.PLAYLIST }
        SyncCollection.SAVED_ARTISTS -> db.artistDao().getSavedArtists().first().isEmpty()
    }

    private fun com.colux.libretune.data.remote.backend.RemoteSong.toEntityStub(now: Long): SongEntity =
        SongEntity(
            songId = remote_id,
            title = title,
            albumId = null,
            images = listOfNotNull(thumbnail_url.toImageAttribute()),
            views = 0L,
            durationSec = if (duration_ms > 0) duration_ms / 1000 else null,
            updateTimestamp = now,
        )

    private fun String.toImageAttribute(): ImageAttribute? =
        if (isBlank()) null else ImageAttribute(url = this)
}

private fun SyncStateResponse.remoteTimestamp(collection: SyncCollection): Long? =
    when (collection) {
        SyncCollection.LIKED_SONGS -> liked_songs
        SyncCollection.PLAYLISTS -> playlists
        SyncCollection.SAVED_ALBUMS -> saved_albums
        SyncCollection.SAVED_ARTISTS -> saved_artists
    }
