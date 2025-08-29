package com.colux.libretune.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromArtistListToJson(artists: List<LikedSongArtist>): String {
        return Json.encodeToString(artists)
    }

    @TypeConverter
    fun fromJsonToArtistList(json: String): List<LikedSongArtist> {
        return Json.decodeFromString(json)
    }


    @TypeConverter
    fun fromImageListToJson(artists: List<LikedSongImage>): String {
        return Json.encodeToString(artists)
    }

    @TypeConverter
    fun fromJsonToImageList(json: String): List<LikedSongImage> {
        return Json.decodeFromString(json)
    }
}