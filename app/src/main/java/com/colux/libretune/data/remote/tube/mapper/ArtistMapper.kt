package com.colux.libretune.data.remote.tube.mapper

import com.coluzziandrea.libretune_extractor.model.GenericMusicItem
import com.colux.libretune.data.model.Artist as DataModelArtist
import com.coluzziandrea.libretune_extractor.model.Artist as ExtractorArtist


fun ExtractorArtist.toDataModel(): DataModelArtist {
    return DataModelArtist(
        id = this.id,
        name = this.name,
        images = this.images.map { it.toDataModel() }
    )
}


fun GenericMusicItem.toArtist(): DataModelArtist? {
    return when (this) {
        is GenericMusicItem.ArtistResult -> this.artist?.toDataModel()
        else -> null
    }
}