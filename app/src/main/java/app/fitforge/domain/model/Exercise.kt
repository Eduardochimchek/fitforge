package app.fitforge.domain.model

import app.fitforge.data.db.ExerciseEntity
import java.text.Normalizer

data class Exercise(
    val id: String,
    val name: String,
    val nameEn: String,
    val category: Category,
    val level: Level,
    val equipment: Equipment,
    val primaryMuscles: List<String>,
    val secondaryMuscles: List<String>,
    val homeFriendly: Boolean,
    val timeBased: Boolean,
    val instructions: List<String>,
    val images: List<String>,
    val tip: String?,
    val translated: Boolean,
    val custom: Boolean,
    val videoUrl: String?,
    val favorite: Boolean,
) {
    val searchKey: String by lazy {
        normalize(buildString {
            append(name).append(' ').append(nameEn).append(' ')
            (primaryMuscles + secondaryMuscles).forEach { append(muscleLabel(it)).append(' ') }
            append(equipment.label).append(' ').append(category.label)
        })
    }

    val youtubeSearchUrl: String
        get() = "https://www.youtube.com/results?search_query=" +
            java.net.URLEncoder.encode("$name execução correta", "UTF-8")
}

fun normalize(s: String): String =
    Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")

fun ExerciseEntity.toDomain() = Exercise(
    id = id,
    name = name,
    nameEn = nameEn,
    category = fromCode(category, Category.MUSCULACAO),
    level = fromCode(level, Level.INICIANTE),
    equipment = fromCode(equipment, Equipment.OUTRO),
    primaryMuscles = primaryMuscles,
    secondaryMuscles = secondaryMuscles,
    homeFriendly = homeFriendly,
    timeBased = timeBased,
    instructions = instructions,
    images = images,
    tip = tip,
    translated = translated,
    custom = custom,
    videoUrl = videoUrl,
    favorite = favorite,
)
