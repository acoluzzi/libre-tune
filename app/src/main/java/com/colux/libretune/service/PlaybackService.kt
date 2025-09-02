package com.colux.libretune.service

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.colux.libretune.data.model.Song
import com.colux.libretune.data.repository.SongRepository
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

    @Inject
    lateinit var musicRepository: SongRepository

    @Inject
    lateinit var exoPlayer: ExoPlayer

    private var mediaSession: MediaSession? = null
    private var backgroundFetchJob: Job? = null

    companion object {
        val COMMAND_PLAY_PLAYLIST_WITH_FETCH =
            SessionCommand("PLAY_PLAYLIST_WITH_FETCH", Bundle.EMPTY)
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
                    val songUrl = musicRepository.getSongUrlById(startingSong.id)

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
                        val songUrl = musicRepository.getSongUrlById(song.id)
                        if (songUrl != null) {
                            val realItem = placeholderMediaItems[index].buildUpon()
                                .setUri(songUrl)
                                .build()
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
        // Pass the callback when building the session
        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setCallback(mediaSessionCallback)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }


    override fun onDestroy() {
        mediaSession?.run {
            exoPlayer.release()
            release()
            mediaSession = null
        }
        backgroundFetchJob?.cancel()
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