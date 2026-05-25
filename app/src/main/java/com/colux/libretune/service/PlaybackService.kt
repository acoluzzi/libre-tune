package com.colux.libretune.service

import android.app.PendingIntent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.colux.libretune.R
import com.colux.libretune.data.model.Song
import com.colux.libretune.data.repository.SongRepository
import com.colux.libretune.ui.MainActivity
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {

    companion object {
        const val NOTIFICATION_ID = 888
        const val CHANNEL_ID = "music_channel_01"
        val COMMAND_PLAY_PLAYLIST_WITH_FETCH =
            SessionCommand("PLAY_PLAYLIST_WITH_FETCH", Bundle.EMPTY)

        // Stream URLs from YouTube are signed and expire after a few hours, so we
        // refresh anything older than this rather than handing a dead URL to the player.
        private const val URL_CACHE_TTL_MS = 5L * 60 * 60 * 1000

        // How many times we re-fetch a fresh URL for the same song before giving up
        // and skipping ahead. Prevents an infinite recovery loop on a permanently
        // broken track.
        private const val MAX_RECOVERY_ATTEMPTS = 2
    }

    @Inject
    lateinit var songRepository: SongRepository

    @Inject
    lateinit var exoPlayer: ExoPlayer

    private var mediaSession: MediaSession? = null
    private var backgroundFetchJob: Job? = null
    private var recoveryJob: Job? = null

    val logger = java.util.logging.Logger.getLogger("PlaybackService")

    private data class CachedUrl(val url: String, val timestampMs: Long)

    private val urlCache = mutableMapOf<String, CachedUrl>()

    // Tracks consecutive failed recovery attempts per song id, reset once the song
    // reaches a playable state.
    private val recoveryAttempts = mutableMapOf<String, Int>()

    /**
     * A helper function that first checks the cache for a URL.
     * If not found (or expired), it fetches from the repository and caches it.
     */
    private suspend fun getOrFetchUrl(song: Song): String? {
        val cached = urlCache[song.id]
        if (cached != null &&
            System.currentTimeMillis() - cached.timestampMs < URL_CACHE_TTL_MS
        ) {
            return cached.url
        }
        return refreshUrl(song.id)
    }

    /**
     * Always fetches a fresh URL from the remote source and updates the cache,
     * bypassing any (possibly stale) cached entry.
     */
    private suspend fun refreshUrl(songId: String): String? {
        val fetchedUrl = songRepository.getSongUrlById(songId)
        if (fetchedUrl != null) {
            urlCache[songId] = CachedUrl(fetchedUrl, System.currentTimeMillis())
        }
        return fetchedUrl
    }

    private val playerListener = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
            recoverFromPlaybackError(error)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            // A song that successfully became playable should get a clean slate of
            // recovery attempts if it later fails again (e.g. its URL expires).
            if (playbackState == Player.STATE_READY) {
                exoPlayer.currentMediaItem?.mediaId?.let { recoveryAttempts.remove(it) }
            }
        }
    }

    /**
     * Recovers from a playback error (most commonly an expired stream URL) by
     * re-fetching a fresh URL for the current song and resuming from the same
     * position. Skips ahead if the song can't be recovered.
     */
    private fun recoverFromPlaybackError(error: PlaybackException) {
        val mediaItem = exoPlayer.currentMediaItem ?: return
        val itemIndex = exoPlayer.currentMediaItemIndex
        val songId = mediaItem.mediaId
        val resumePosition = exoPlayer.currentPosition.coerceAtLeast(0L)

        val attempts = recoveryAttempts.getOrDefault(songId, 0)
        if (attempts >= MAX_RECOVERY_ATTEMPTS) {
            logger.warning {
                "Giving up on $songId after $attempts recovery attempts (${error.errorCodeName}); skipping."
            }
            skipToNextOrStop()
            return
        }
        recoveryAttempts[songId] = attempts + 1

        recoveryJob?.cancel()
        recoveryJob = CoroutineScope(Dispatchers.Main).launch {
            logger.info { "Playback error (${error.errorCodeName}); refreshing URL for $songId" }
            val freshUrl = refreshUrl(songId)
            if (freshUrl != null) {
                val refreshedItem = mediaItem.buildUpon().setUri(freshUrl).build()
                exoPlayer.replaceMediaItem(itemIndex, refreshedItem)
                exoPlayer.prepare()
                exoPlayer.seekTo(itemIndex, resumePosition)
                exoPlayer.play()
            } else {
                skipToNextOrStop()
            }
        }
    }

    private fun skipToNextOrStop() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    private val mediaSessionCallback = object : MediaSession.Callback {

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            // Get the default available commands
            val availableSessionCommands =
                super.onConnect(session, controller).availableSessionCommands.buildUpon()

            // Add our custom command to the list of allowed commands
            availableSessionCommands.add(COMMAND_PLAY_PLAYLIST_WITH_FETCH)

            // Return the result with the updated command list
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(availableSessionCommands.build())
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction == COMMAND_PLAY_PLAYLIST_WITH_FETCH.customAction) {
                backgroundFetchJob?.cancel()
                val playlist = args.getParcelableArrayList<Song>("PLAYLIST")!!
                val startingIndex = args.getInt("START_INDEX")

                // 1. Create the placeholder list, but DON'T give it to the player yet.
                val placeholderMediaItems = playlist.map { it.toPlaceholderMediaItem() }

                // 2. Clear the old playlist and add the new placeholders.
                exoPlayer.setMediaItems(placeholderMediaItems, startingIndex, 0L)
                // 3. DO NOT CALL prepare() or play() here yet.

                backgroundFetchJob = CoroutineScope(Dispatchers.Main).launch {
                    // 4. Fetch the URL for the song that needs to start playing NOW.
                    val startingSong = playlist[startingIndex]

                    logger.info { "Fetching URL for starting song: ${startingSong.title}" }
                    val songUrl = getOrFetchUrl(startingSong)

                    if (songUrl != null) {
                        // 5. Create a real MediaItem with the fetched URL.
                        val realMediaItem = placeholderMediaItems[startingIndex].buildUpon()
                            .setUri(songUrl)
                            .build()

                        // 6. Replace the placeholder for the starting song.
                        exoPlayer.replaceMediaItem(startingIndex, realMediaItem)

                        // 7. NOW that the first song is ready, prepare and play.
                        exoPlayer.prepare()

                        exoPlayer.play()
                    }

                    // 8. Continue fetching the rest of the playlist in the background.
                    playlist.forEachIndexed { index, song ->
                        if (index == startingIndex) return@forEachIndexed

                        logger.info { "Fetching URL for remain song: ${song.title}" }
                        val songUrl = getOrFetchUrl(song)
                        if (songUrl != null) {
                            val realItem = placeholderMediaItems[index].buildUpon()
                                .setUri(songUrl)
                                .build()
                            logger.info { "Replaced URL for remain song: ${song.title}" }
                            exoPlayer.replaceMediaItem(index, realItem)
                        }
                    }
                }
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }


    override fun onCreate() {
        super.onCreate()

        exoPlayer.addListener(playerListener)

        // 1. Create the PendingIntent that opens your MainActivity.
        val pendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                // Add the MainActivity class explicitly for clarity and robustness
                sessionIntent.setClass(this, MainActivity::class.java)
                PendingIntent.getActivity(this, 0, sessionIntent, PendingIntent.FLAG_IMMUTABLE)
            }


        // 2. Build the MediaSession with the PendingIntent.
        mediaSession = pendingIntent?.let {
            MediaSession.Builder(this, exoPlayer)
                .setCallback(mediaSessionCallback)
                .setSessionActivity(it)
        }
            ?.build()

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider(
                this,
                { NOTIFICATION_ID },
                CHANNEL_ID,
                R.string.music_channel_name,
            )
                .apply {
                    setSmallIcon(R.drawable.ic_notification)
                })
    }


    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }


    override fun onDestroy() {
        mediaSession?.run {
            exoPlayer.removeListener(playerListener)
            exoPlayer.release()
            release()
            mediaSession = null
        }
        backgroundFetchJob?.cancel()
        recoveryJob?.cancel()
        super.onDestroy()
    }
}

// Helper to create placeholder MediaItems
fun Song.toPlaceholderMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(id)
        .setUri(id)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(getArtistNames())
                .setArtworkUri(android.net.Uri.parse(getBestImageUrl()))
                .build()
        )
        .build()
}