package com.colux.libretune.data.model

sealed interface SearchResult {
    data class SongResult(val song: Song) : SearchResult
    data class ArtistResult(val artist: Artist) : SearchResult
}