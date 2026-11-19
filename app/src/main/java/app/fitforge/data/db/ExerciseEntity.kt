package app.fitforge.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "name_en") val nameEn: String,
    val category: String,
    val level: String,
    val equipment: String,
    @ColumnInfo(name = "primary_muscles") val primaryMuscles: List<String>,
    @ColumnInfo(name = "secondary_muscles") val secondaryMuscles: List<String>,
    @ColumnInfo(name = "home_friendly") val homeFriendly: Boolean,
    @ColumnInfo(name = "time_based") val timeBased: Boolean,
    val instructions: List<String>,
    val images: List<String>,
    val tip: String?,
    val translated: Boolean,
    val custom: Boolean = false,
    @ColumnInfo(name = "video_url") val videoUrl: String? = null,
    val favorite: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = 0,
)
