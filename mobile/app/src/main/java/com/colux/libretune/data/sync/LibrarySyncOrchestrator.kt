package com.colux.libretune.data.sync

import androidx.room.withTransaction
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
import com.colux.libretune.data.repository.BackendSyncRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.first
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Owns the local⇄remote synchronisation policy.
 *
 * The local Room database is the source of truth, so each batch run looks at
 * each [SyncCollection] independently and decides what to do based on the
 * timestamps recorded in [SyncMetadataStore]:
 *
 * - **Local is empty** → pull the remote snapshot into the local database
 *   (new device / fresh install).
 * - **Local was modified after last sync** (or never synced) → push the
 *   local snapshot to the backend.
 * - **Otherwise** → no-op.
 *
 * Push always sends the full snapshot for the collection so the server
 * can replace its state — there is no partial diff protocol on the wire.
 */
@Singleton
class LibrarySyncOrchestrator @Inject constructor(
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
        SyncCollection.values().forEach { collection ->
            try {
                syncCollection(collection)
            } catch (t: Throwable) {
                logger.log(Level.WARNING, "Sync failed for $collection", t)
            }
        }
    }

    suspend fun syncCollection(collection: SyncCollection) {
        if (!backend.isAuthenticated()) return
        val localEmpty = isLocalEmpty(collection)
        val neverSynced = metadata.syncedAt(collection) == 0L
        when {
            localEmpty -> {
                logger.info("Pulling $collection (local is empty).")
                pull(collection)
                metadata.setSyncedAt(collection)
            }
            metadata.isDirty(collection) || neverSynced -> {
                logger.info("Pushing $collection (local dirty or never synced).")
                push(collection)
                metadata.setSyncedAt(collection)
            }
            else -> logger.info("Nothing to sync for $collection.")
        }
    }

    // ---------------------------------------------------------------------
    // PUSH
    // ---------------------------------------------------------------------

    private suspend fun push(collection: SyncCollection) {
        when (collection) {
            SyncCollection.LIKED_SONGS -> backend.pushLikedSongs(buildLikedSongsPayload())
            SyncCollection.PLAYLISTS -> backend.pushPlaylists(buildPlaylistsPayload())
            SyncCollection.SAVED_ALBUMS -> backend.pushSavedAlbums(buildSavedAlbumsPayload())
            SyncCollection.SAVED_ARTISTS -> backend.pushSavedArtists(buildSavedArtistsPayload())
        }
    }

    private suspend fun buildLikedSongsPayload(): LikedSongsPayload {
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
        return LikedSongsPayload(items)
    }

    private suspend fun buildPlaylistsPayload(): PlaylistsPayload {
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
        return PlaylistsPayload(items)
    }

    private suspend fun buildSavedAlbumsPayload(): SavedAlbumsPayload {
        val albums = db.playlistDao().getSavedPlaylists().first()
            .filter { it.playlist.type != AlbumType.PLAYLIST }
        val items = albums.mapIndexed { index, pws ->
            pws.playlist.toRemoteAlbum(
                artistName = pws.artists.joinToString(", ") { it.name },
                position = index,
            )
        }
        return SavedAlbumsPayload(items)
    }

    private suspend fun buildSavedArtistsPayload(): SavedArtistsPayload {
        val artists = db.artistDao().getSavedArtists().first()
        val items = artists.mapIndexed { index, artist ->
            artist.toRemoteArtist(position = index)
        }
        return SavedArtistsPayload(items)
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
        if (payload.items.isEmpty()) return
        val now = System.currentTimeMillis()
        db.withTransaction {
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
        if (payload.items.isEmpty()) return
        val now = System.currentTimeMillis()
        db.withTransaction {
            payload.items.forEach { remotePlaylist ->
                val playlistId = remotePlaylist.remote_id.ifEmpty { "remote-${now}-${remotePlaylist.name.hashCode()}" }
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
        if (payload.items.isEmpty()) return
        val now = System.currentTimeMillis()
        db.withTransaction {
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
        if (payload.items.isEmpty()) return
        val now = System.currentTimeMillis()
        db.withTransaction {
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
