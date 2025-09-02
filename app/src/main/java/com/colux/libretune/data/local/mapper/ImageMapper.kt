package com.colux.libretune.data.local.mapper

import com.colux.libretune.data.local.entity.ImageAttribute
import com.colux.libretune.data.model.Image

fun ImageAttribute.toDataModel(): Image {
    return Image(
        url = this.url,
        width = this.width,
        height = this.height
    )
}

fun Image.toEntity(): ImageAttribute {
    return ImageAttribute(
        url = this.url,
        width = this.width,
        height = this.height
    )
}