package com.colux.libretune.ui.player

import android.content.ComponentName
import android.content.Context
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.colux.libretune.data.local.LikedSongDao
import com.colux.libretune.data.local.LikedSongEntity
import com.colux.libretune.data.model.Song
import com.colux.libretune.data.repository.MusicRepository
import com.colux.libretune.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val musicRepository: MusicRepository,
    private val likedSongDao: LikedSongDao,
) : ViewModel() {

    private var mediaController: MediaController? = null
    private lateinit var controllerFuture: ListenableFuture<MediaController>

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration: StateFlow<Long> = _totalDuration.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private var progressTrackingJob: Job? = null

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken =
            SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            mediaController?.addListener(playerListener)
            updateStateWithController()
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlayingValue: Boolean) {
            _isPlaying.value = isPlayingValue
            if (isPlayingValue) {
                startProgressTracking()
            } else {
                stopProgressTracking()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateStateWithController()
        }


        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.value = repeatMode
        }
    }

    private fun updateStateWithController() {
        mediaController?.let {
            _currentSong.value = it.currentMediaItem?.toSong()
            _totalDuration.value = it.duration.coerceAtLeast(0L)
            _isPlaying.value = it.isPlaying
            _repeatMode.value = it.repeatMode
            // Ensure progress tracking is correct based on the latest state
            if (it.isPlaying) startProgressTracking() else stopProgressTracking()
        }
    }

    fun playPlaylist(playlist: List<Song>, startingIndex: Int) {
        val args = bundleOf(
            "PLAYLIST" to ArrayList(playlist),
            "START_INDEX" to startingIndex
        )
        mediaController?.sendCustomCommand(PlaybackService.COMMAND_PLAY_PLAYLIST_WITH_FETCH, args)
    }

    private fun startProgressTracking() {
        stopProgressTracking()
        // Start a new job
        progressTrackingJob = viewModelScope.launch {
            while (true) {
                val currentPosition = mediaController?.currentPosition?.coerceAtLeast(0L) ?: 0L
                _currentPosition.value = currentPosition
                delay(500)
            }
        }
    }

    private fun stopProgressTracking() {
        progressTrackingJob?.cancel()
    }

    // --- Standard Player Controls ---
    fun onPlayPauseClick() {
        if (mediaController?.isPlaying == true) mediaController?.pause() else mediaController?.play()
    }

    fun playNextSong() = mediaController?.seekToNextMediaItem()
    fun playPreviousSong() = mediaController?.seekToPreviousMediaItem()
    fun seekToPosition(position: Long) = mediaController?.seekTo(position)


    fun toggleRepeat() {
        mediaController?.let {
            val nextRepeatMode = when (it.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
            it.setRepeatMode(nextRepeatMode)
        }
    }


    // --- Liked Songs Logic (can remain in ViewModel) ---
    fun isCurrentSongLiked(songId: String): Flow<Boolean> = likedSongDao.isLiked(songId)
    fun onLikeClick(song: Song, isLiked: Boolean) {
        viewModelScope.launch {
            val entity = LikedSongEntity(song.id, song.title, song.artist ?: "", song.imageUrl)
            if (isLiked) likedSongDao.unlikeSong(entity) else likedSongDao.likeSong(entity)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressTracking()
        mediaController?.let {
            it.removeListener(playerListener)
            MediaController.releaseFuture(controllerFuture)
        }
    }
}

// Helper to convert a MediaItem back to a Song for the UI
fun MediaItem.toSong(): Song? {
    return if (mediaMetadata.title != null) {
        Song(
            id = mediaId,
            title = mediaMetadata.title.toString(),
            artist = mediaMetadata.artist.toString(),
            imageUrl = mediaMetadata.artworkUri.toString(),
            mediaUrl = null
        )
    } else {
        null
    }
}