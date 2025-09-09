package com.colux.libretune.ui.player

import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import com.colux.libretune.data.model.PlaylistDetails
import com.colux.libretune.data.model.Song
import com.colux.libretune.data.repository.SongRepository
import com.colux.libretune.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.logging.Logger
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songRepository: SongRepository,
) : ViewModel() {


    val logger = Logger.getLogger(PlayerViewModel::class.java.name)

    private var mediaController: MediaController? = null
    private lateinit var controllerFuture: ListenableFuture<MediaController>

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<Song>>(emptyList())

    private val _currentPlaylistId = MutableStateFlow<String?>(null)
    val currentPlaylistId: StateFlow<String?> = _currentPlaylistId.asStateFlow()

    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration: StateFlow<Long> = _totalDuration.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private var progressTrackingJob: Job? = null

    private val _dynamicPrimaryColor = MutableStateFlow<Color?>(null)
    val dynamicPrimaryColor: StateFlow<Color?> = _dynamicPrimaryColor.asStateFlow()


    val savedSongIds: StateFlow<List<String>> = songRepository.getSavedSongIds()
        .onEach { newList ->
            logger.info {
                "Saved song IDs updated: $newList"
            }
        }.stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
        override fun onEvents(player: Player, events: Player.Events) {
            updateStateWithController()
        }
    }


    private fun updateStateWithController() {
        mediaController?.let { controller ->

            val oldSongId = _currentSong.value?.id
            val newSongId = controller.currentMediaItem?.mediaId
            val newSong = _currentPlaylist.value.find {
                it.id == newSongId
            }
            val isSongChanged = oldSongId != newSongId && newSongId != null
            val currentPosition = mediaController?.currentPosition?.coerceAtLeast(0L) ?: 0L

            if (isSongChanged) {
                newSong?.let {
                    viewModelScope.launch {
                        logger.info { "Song changed from $oldSongId to $newSongId" }
                        extractColorFromSong(it)
                        songRepository.logSongPlayed(it)
                    }
                }
            }

            _isPlaying.value = controller.isPlaying
            _currentSong.value = newSong
            _totalDuration.value = controller.duration.coerceAtLeast(0L)
            _repeatMode.value = controller.repeatMode
            _currentPosition.value = currentPosition

            // Manage the progress tracking based on the latest isPlaying state
            if (controller.isPlaying) {
                startProgressTracking()
            } else {
                stopProgressTracking()
            }
        }
    }

    fun playSongList(playlist: List<Song>, startingIndex: Int = 0) {
        _playSongList(playlist, startingIndex)
        _currentPlaylistId.value = null
    }

    private fun _playSongList(playlist: List<Song>, startingIndex: Int) {
        val args = bundleOf(
            "PLAYLIST" to ArrayList(playlist),
            "START_INDEX" to startingIndex
        )
        _currentPlaylist.value = playlist
        mediaController?.sendCustomCommand(PlaybackService.COMMAND_PLAY_PLAYLIST_WITH_FETCH, args)
    }


    fun playPlaylist(playlistDetails: PlaylistDetails, startingIndex: Int = 0) {
        _playSongList(playlistDetails.songs, startingIndex)

        _currentPlaylistId.value = playlistDetails.id
    }


    fun shufflePlayPlaylist(playlistDetails: PlaylistDetails) {
        _playSongList(
            playlistDetails.songs.shuffled(),
            0
        )

        _currentPlaylistId.value = playlistDetails.id
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

    fun isSongInLocalPlaylist(songId: String, playlistId: String? = null): Flow<Boolean> {
        if (playlistId == null) {
            return flowOf(false)
        }
        return songRepository.isSongInLocalPlaylist(playlistId, songId)
    }


    // --- Liked Songs Logic (can remain in ViewModel) ---
    fun isCurrentSongLiked(songId: String): Flow<Boolean> = songRepository.isSongLiked(songId)

    fun onLikeClick(song: Song, isLiked: Boolean) {
        viewModelScope.launch {
            logger.info { "Like clicked on $song, isLiked: $isLiked" }
            if (isLiked) {
                songRepository.unlikeSong(song)
            } else {
                songRepository.likeSong(song)
            }
        }
    }

    fun removeSongFromPlaylist(song: Song, playlistId: String) {
        viewModelScope.launch {
            songRepository.removeSongFromPlaylist(playlistId, song.id)
        }
    }

    private fun extractColorFromSong(song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageLoader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(song.getBestImageUrl())
                    .allowHardware(false)
                    .build()
                val image = (imageLoader.execute(request).drawable as? BitmapDrawable)?.bitmap
                    ?: return@launch

                val vibrantSwatch = Palette.from(image).generate().vibrantSwatch

                // Set the dynamic primary color, or null if no vibrant swatch found
                _dynamicPrimaryColor.value = vibrantSwatch?.rgb?.let { Color(it) }

            } catch (e: Exception) {
                _dynamicPrimaryColor.value = null // Reset to null on error to use default theme
                e.printStackTrace()
            }
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


