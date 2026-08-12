package com.zafar.ichatai.data.local

import android.net.Uri
import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromUri(uri: Uri?): String? {
        return uri?.toString()
    }

    @TypeConverter
    fun toUri(uriString: String?): Uri? {
        return uriString?.let { Uri.parse(it) }
    }

    @TypeConverter
    fun fromUriList(uris: List<Uri>?): String? {
        return uris?.joinToString(",") { it.toString() }
    }

    @TypeConverter
    fun toUriList(uriString: String?): List<Uri>? {
        if (uriString.isNullOrBlank()) return emptyList()
        return uriString.split(",").map { Uri.parse(it) }
    }
}
