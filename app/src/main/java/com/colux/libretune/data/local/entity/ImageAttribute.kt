package com.colux.libretune.data.local.entity

import kotlinx.serialization.Serializable


@Serializable
data class ImageAttribute(
    val url: String,
    val width: Int? = null,
    val height: Int? = null
)