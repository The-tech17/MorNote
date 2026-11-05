package com.sanjana.mornote

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    @Suppress("unused")
    fun fromList(list: List<String>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    @Suppress("unused")
    fun toList(data: String): List<String> {
        return if (data.isBlank()) emptyList() else data.split(",")
    }
}
