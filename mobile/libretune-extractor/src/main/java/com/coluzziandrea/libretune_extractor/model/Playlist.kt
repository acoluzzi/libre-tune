package com.coluzziandrea.libretune_extractor.model

enum class PlaylistType {
    PLAYLIST,
    ALBUM,
    SINGLE,
    EP
}

data class Playlist(
    val node: MusicNode,
    val images: List<Image>,
    val artists: List<Artist>? = null,
    val type: PlaylistType? = PlaylistType.PLAYLIST,
    val releaseYear: Int? = null,
) {

    val name: String
        get() = node.name

    val id: String
        get() = node.id

    constructor(
        id: String,
        name: String,
        images: List<Image>,
        artists: List<Artist>? = null,
        type: PlaylistType = PlaylistType.PLAYLIST,
        releaseYear: Int? = null
    ) : this(
        node = MusicNode(
            id = id,
            name = name
        ),
        images = images,
        artists = artists,
        type = type,
        releaseYear = releaseYear
    )

}