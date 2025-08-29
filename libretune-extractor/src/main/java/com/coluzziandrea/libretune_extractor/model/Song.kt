package com.coluzziandrea.libretune_extractor.model

data class Song(
    val id: String,
    val artist: String,
    val playlistId: String?,
    val title: String,
    val images: List<Image>,
)