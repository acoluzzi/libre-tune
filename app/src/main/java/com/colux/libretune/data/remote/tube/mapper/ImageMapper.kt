package com.colux.libretune.data.remote.tube.mapper

import com.colux.libretune.data.model.Image as DataModelImage
import com.coluzziandrea.libretune_extractor.model.Image as ExtractorImage


fun ExtractorImage.toDataModel(): DataModelImage {
    return DataModelImage(
        url = this.url,
        width = this.width,
        height = this.height
    )
}