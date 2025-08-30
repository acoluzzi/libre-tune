package com.colux.libretune.data.local

import androidx.room.TypeConverter
import com.colux.libretune.data.local.entity.AlbumType
import com.colux.libretune.data.local.entity.ImageAttribute
import kotlinx.serialization.json.Json

class Converters {

    @TypeConverter
    fun fromImageListToJson(artists: List<ImageAttribute>): String {
        return Json.encodeToString(artists)
    }

    @TypeConverter
    fun fromJsonToImageList(json: String): List<ImageAttribute> {
        return Json.decodeFromString(json)
    }

    @TypeConverter
    fun fromAlbumType(type: AlbumType): String = type.name

    @TypeConverter
    fun toAlbumType(name: String): AlbumType = AlbumType.valueOf(name)
}