package com.colux.libretune.data.model

data class Song(
    val id: String,
    val title: String,
    val artist: String?,
    val imageUrl: String,
    val mediaUrl: String?,
)