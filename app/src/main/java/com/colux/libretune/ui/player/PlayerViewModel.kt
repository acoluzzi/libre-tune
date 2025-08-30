package com.colux.libretune.ui.player

import android.content.ComponentName
import android.content.Context
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.colux.libretune.data.local.dao.SongDao
import com.colux.libretune.data.model.Song
import com.colux.libretune.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val likedSongDao: SongDao,
) : ViewModel() {

    private var mediaController: MediaController? = null
    private lateinit var controllerFuture: ListenableFuture<MediaController>

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<Song>>(emptyList())

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
        // This single callback is fired for any and all player state changes.
        override fun onEvents(player: Player, events: Player.Events) {
            // We just refresh our entire state from the controller.
            updateStateWithController()
        }
    }


    private fun updateStateWithController() {
        mediaController?.let { controller ->
            _isPlaying.value = controller.isPlaying
            _currentSong.value = _currentPlaylist.value.find {
                it.id == controller.currentMediaItem?.mediaId
            }
            _totalDuration.value = controller.duration.coerceAtLeast(0L)
            _repeatMode.value = controller.repeatMode

            // Manage the progress tracking based on the latest isPlaying state
            if (controller.isPlaying) {
                startProgressTracking()
            } else {
                stopProgressTracking()
            }
        }
    }

    fun playPlaylist(playlist: List<Song>, startingIndex: Int) {
        val args = bundleOf(
            "PLAYLIST" to ArrayList(playlist),
            "START_INDEX" to startingIndex
        )
        _currentPlaylist.value = playlist
        mediaController?.sendCustomCommand(PlaybackService.COMMAND_PLAY_PLAYLIST_WITH_FETCH, args)
    }

    private fun startProgressTracking() {
        if (progressTrackingJob?.isActive == true) return

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
//    fun isCurrentSongLiked(songId: String): Flow<Boolean> = likedSongDao.isLiked(songId)
//    fun onLikeClick(song: Song, isLiked: Boolean) {
//        viewModelScope.launch {
//            val entity = LikedSongEntity.from(song)
//            if (isLiked) likedSongDao.unlikeSong(entity) else likedSongDao.likeSong(entity)
//        }
//    }

    override fun onCleared() {
        super.onCleared()
        stopProgressTracking()
        mediaController?.let {
            it.removeListener(playerListener)
            MediaController.releaseFuture(controllerFuture)
        }
    }


}


