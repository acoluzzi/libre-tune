package com.colux.libretune.data.model

/**
 * A sealed interface to represent any type of suggestion.
 */
sealed interface SearchSuggestion {
    fun getImageForList(): String?
    fun getName(): String
    fun getArtistNameLabel(): String?

    /**
     * Represents a simple text query suggestion.
     */
    data class QuerySuggestion(
        val query: String,
        val isFromHistory: Boolean
    ) : SearchSuggestion {
        override fun getImageForList(): String? = null
        override fun getName(): String = query
        override fun getArtistNameLabel(): String? = null
    }

    /**
     * Represents a specific entity (Song, Artist, etc.).
     */
    data class EntitySuggestion(
        val song: Song? = null,
        val artist: Artist? = null,
        val album: Playlist? = null,
        val playlist: Playlist? = null,
        val type: String,
    ) : SearchSuggestion {
        override fun getImageForList(): String? {
            return song?.images?.firstOrNull()?.url
                ?: artist?.images?.firstOrNull()?.url
                ?: album?.images?.firstOrNull()?.url
                ?: playlist?.images?.firstOrNull()?.url
        }

        override fun getName(): String {
            return song?.title ?: artist?.name ?: album?.name ?: playlist?.name ?: "Unknown"
        }

        override fun getArtistNameLabel(): String? {
            return when {
                song != null -> song.getArtistNames()
                artist != null -> null
                album != null -> album.getArtistNames()
                playlist != null -> playlist.getArtistNames()
                else -> null
            }
        }
    }
}