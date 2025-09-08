package com.coluzziandrea.libretune_extractor.model


data class Song(
    val node: MusicNode,
    val artists: List<Artist>,
    val album: Playlist? = null,
    val playlistId: String?,
    val images: List<Image>,
    val views: Long,
    val durationSec: Long? = null
) {
    val title: String
        get() = node.name

    val id: String
        get() = node.id

    constructor(
        id: String,
        title: String,
        artists: List<Artist>,
        album: Playlist? = null,
        playlistId: String?,
        images: List<Image>,
        views: Long,
        durationSec: Long? = null
    ) : this(
        node = MusicNode(
            id = id,
            name = title
        ),
        artists = artists,
        album = album,
        playlistId = playlistId,
        images = images,
        views = views,
        durationSec = durationSec
    )

}