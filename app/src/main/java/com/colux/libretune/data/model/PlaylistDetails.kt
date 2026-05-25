package com.colux.libretune.data.model

data class PlaylistDetails(
    val id: String,
    val name: String,
    val isLocal: Boolean,
    val type: PlaylistType,
    val images: List<Image>,
    val artists: List<Artist>,
    val songs: List<Song>,
    val relatedPlaylists: List<Playlist>,
    val releaseYear: Int,
    val syncEnabled: Boolean = false,
    val remotePlaylistId: String? = null,
) {
    val totalDurationSeconds: Long
        get() = songs.sumOf { it.durationSec ?: 0L }

    fun bestImage(): String? {
        return images.maxByOrNull { it.width ?: 0 }?.url
    }

    fun getArtistNames(): String {
        return artists.joinToString(", ") { it.name }
    }

    fun getFormattedTotalDuration(): String {
        val totalSeconds = songs.sumOf { it.durationSec ?: 0L }
        if (totalSeconds == 0L) return ""
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format("%d hr %d min", hours, minutes)
        } else {
            String.format("%d min %d secs", minutes, seconds)
        }
    }
}