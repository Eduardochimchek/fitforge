package app.fitforge.data.db

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromList(value: List<String>): String = json.encodeToString(value)

    @TypeConverter
    fun toList(value: String): List<String> =
        if (value.isBlank()) emptyList() else json.decodeFromString(value)
}
