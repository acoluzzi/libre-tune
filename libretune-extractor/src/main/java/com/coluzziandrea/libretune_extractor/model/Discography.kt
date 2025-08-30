package com.coluzziandrea.libretune_extractor.model

data class Discography(
    val albums: List<Playlist>,
    val singlesAndEp: List<Playlist>,
)