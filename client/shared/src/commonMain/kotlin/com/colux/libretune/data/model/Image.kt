package com.colux.libretune.data.model

import com.colux.libretune.shared.parcelable.Parcelable
import com.colux.libretune.shared.parcelable.Parcelize


@Parcelize
data class Image(
    val url: String,
    val width: Int?,
    val height: Int?
) : Parcelable