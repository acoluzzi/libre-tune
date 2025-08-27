package com.coluzziandrea.libretune_extractor.model

data class Song(
    val id: String,
    val playlistId: String?,
    val title: String,
    val imageUrl: String?,
)