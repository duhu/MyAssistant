package com.myassistant.app.data.local.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    @TypeConverter
    fun fromTagsList(tags: List<String>): String = Json.encodeToString(tags)

    @TypeConverter
    fun toTagsList(json: String): List<String> =
        runCatching { Json.decodeFromString<List<String>>(json) }.getOrDefault(emptyList())
}
