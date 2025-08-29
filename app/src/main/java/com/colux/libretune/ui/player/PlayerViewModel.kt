package com.colux.libretune.ui.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.colux.libretune.data.local.LikedSongDao
import com.colux.libretune.data.local.LikedSongEntity
import com.colux.libretune.data.model.Song
import com.colux.libretune.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val musicRepository: MusicRepository,
    private val likedSongDao: LikedSongDao,
) : ViewModel() {

    private var exoPlayer: ExoPlayer? = null
    private var currentPlaylist: List<Song> = emptyList()

    private var backgroundFetchJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration: StateFlow<Long> = _totalDuration
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled
    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode
    private var progressTrackingJob: Job? = null


    init {
        initializePlayer()
    }

    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                    _isPlaying.value = isPlayingValue
                    if (isPlayingValue) startProgressTracking() else stopProgressTracking()
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    val newIndex = this@apply.currentMediaItemIndex
                    if (newIndex >= 0 && newIndex < currentPlaylist.size) {
                        _currentSong.value = currentPlaylist[newIndex]
                        _totalDuration.value = this@apply.duration
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        _totalDuration.value = this@apply.duration
                    }
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    _isShuffleEnabled.value = shuffleModeEnabled
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    _repeatMode.value = repeatMode
                }
            })
        }
    }


    fun playSongFromPlaylist(playlist: List<Song>, startingIndex: Int) {
        // Cancel any previous background fetching
        backgroundFetchJob?.cancel()

        // Immediately update the current song in the UI
        _currentSong.value = playlist.getOrNull(startingIndex)
        currentPlaylist = playlist

        // Create placeholder MediaItems for the entire playlist.
        // These only contain metadata and can't be played yet.
        val placeholderMediaItems = playlist.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id)
                .setUri(song.id)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setArtworkUri(android.net.Uri.parse(song.imageUrl))
                        .build()
                )
                .build()
        }

        // Set the full list of placeholders in ExoPlayer
        exoPlayer?.setMediaItems(placeholderMediaItems, startingIndex, 0L)
        exoPlayer?.prepare()

        // Start the background job to fetch real URLs
        backgroundFetchJob = viewModelScope.launch {
            // 1. Fetch the selected song first to start playback ASAP
            val startingSong = playlist[startingIndex]
            val fullSong = musicRepository.getSongById(startingSong.id)
            if (fullSong?.mediaUrl != null) {
                val realMediaItem = placeholderMediaItems[startingIndex].buildUpon()
                    .setUri(fullSong.mediaUrl)
                    .build()

                // Replace the placeholder with the real item and play
                exoPlayer?.replaceMediaItem(startingIndex, realMediaItem)
                exoPlayer?.play()
            }

            // 2. Fetch the rest of the playlist in the background
            playlist.forEachIndexed { index, song ->
                // Skip the one we just fetched
                if (index == startingIndex) return@forEachIndexed

                val songWithUrl = musicRepository.getSongById(song.id)
                if (songWithUrl?.mediaUrl != null) {
                    val realItem = placeholderMediaItems[index].buildUpon()
                        .setUri(songWithUrl.mediaUrl)
                        .build()
                    // Silently replace the placeholder in the queue
                    exoPlayer?.replaceMediaItem(index, realItem)
                }
            }
        }
    }


    fun onPlayPauseClick() {
        if (exoPlayer?.isPlaying == true) {
            exoPlayer?.pause()
        } else {
            exoPlayer?.play()
        }
    }


    fun toggleRepeat() {
        val nextRepeatMode = when (exoPlayer?.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        exoPlayer?.repeatMode = nextRepeatMode
    }


    private fun startProgressTracking() {
        progressTrackingJob = viewModelScope.launch {
            while (isPlaying.value) {
                _currentPosition.value = exoPlayer?.currentPosition ?: 0L
                delay(500) // Update every 500ms
            }
        }
    }

    fun isCurrentSongLiked(songId: String): Flow<Boolean> {
        return likedSongDao.isLiked(songId)
    }

    fun onLikeClick(song: Song, isLiked: Boolean) {
        viewModelScope.launch {
            val entity = LikedSongEntity(song.id, song.title, song.artist ?: "", song.imageUrl)
            if (isLiked) {
                likedSongDao.unlikeSong(entity)
            } else {
                likedSongDao.likeSong(entity)
            }
        }
    }

    fun playNextSong() {
        exoPlayer?.seekToNextMediaItem()
        _currentSong.value = currentPlaylist[exoPlayer?.currentMediaItemIndex ?: 0]
    }

    fun playPreviousSong() {
        exoPlayer?.seekToPreviousMediaItem()
        _currentSong.value = currentPlaylist[exoPlayer?.currentMediaItemIndex ?: 0]
    }

    private fun stopProgressTracking() {
        progressTrackingJob?.cancel()
    }

    fun seekToPosition(position: Long) {
        exoPlayer?.seekTo(position)
    }


    // Release the player when the ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        stopProgressTracking()
        backgroundFetchJob?.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }
}