package com.colux.libretune.ui.player

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
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


    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    // Placeholder for the currently playing song
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration: StateFlow<Long> = _totalDuration

    // New state for current progress
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode

    private var job: Job? = null


    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlayingValue: Boolean) {
                    _isPlaying.value = isPlayingValue
                    // Start or stop the progress tracking job
                    if (isPlayingValue) {
                        startProgressTracking()
                    } else {
                        stopProgressTracking()
                    }
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    Log.d(
                        "PlayerViewModel",
                        "Media item transitioned: ${mediaItem?.mediaId}, reason: $reason"
                    )
                    // When a new song starts, update the duration
                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                        // Update the UI with the new song's info
                        _currentSong.value = currentPlaylist[exoPlayer?.currentMediaItemIndex ?: 0]
                        _totalDuration.value = exoPlayer?.duration ?: 0L
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        // The player is ready, update duration
                        _totalDuration.value = exoPlayer?.duration ?: 0L
                    }
                }

                // Add new listeners for shuffle and repeat
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
        if (exoPlayer == null) {
            initializePlayer()
        }
        currentPlaylist = playlist
        _currentSong.value = currentPlaylist[startingIndex] // Update current song

        val mediaItems =
            playlist.filter { it.mediaUrl != null }.map { MediaItem.fromUri(it.mediaUrl!!) }
        exoPlayer?.setMediaItems(mediaItems, startingIndex, 0L)
        exoPlayer?.prepare()
        exoPlayer?.play()
    }

    fun playSongById(id: String) {
        viewModelScope.launch {


            // This calls the repository to get the FULL song details, including mediaUrl
            val song = musicRepository.getSongById(id)

            Log.d("PlayerViewModel", "Song details: $song")

            if (song?.mediaUrl != null) {
                if (exoPlayer == null) {
                    initializePlayer()
                }
                Log.d("PlayerViewModel", "Playing song with media URL: ${song.mediaUrl}")
                // Now we have the URL and can play it
                _currentSong.value = song
                exoPlayer?.setMediaItem(MediaItem.fromUri(song.mediaUrl))
                exoPlayer?.prepare()
                exoPlayer?.play()
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

    fun toggleShuffle() {
        exoPlayer?.shuffleModeEnabled = !exoPlayer!!.shuffleModeEnabled
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
        job = viewModelScope.launch {
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
        job?.cancel()
    }

    fun seekToPosition(position: Long) {
        exoPlayer?.seekTo(position)
    }

    fun onLikeClick(song: Song) {
        viewModelScope.launch {
            likedSongDao.likeSong(
                LikedSongEntity(song.id, song.title, song.artist ?: "", song.imageUrl)
            )
        }
    }

    // Release the player when the ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        stopProgressTracking()
        exoPlayer?.release()
    }
}