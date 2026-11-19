package app.fitforge.data.seed

import android.content.Context
import app.fitforge.data.db.ExerciseDao
import app.fitforge.data.db.ExerciseEntity
import app.fitforge.data.prefs.UserPrefsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class SeedFile(val versao: Int, val exercicios: List<SeedExercise>)

@Serializable
private data class SeedExercise(
    val id: String,
    val nome: String,
    val nomeEn: String,
    val categoria: String,
    val nivel: String,
    val equipamento: String,
    val principais: List<String>,
    val secundarios: List<String>,
    val casa: Boolean,
    val porTempo: Boolean,
    val instrucoes: List<String>,
    val traduzido: Boolean,
    val imagens: List<String>,
    val dica: String? = null,
)

@Singleton
class ExerciseSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: ExerciseDao,
    private val prefs: UserPrefsRepository,
    private val json: Json,
) {
    suspend fun seedIfNeeded() = withContext(Dispatchers.IO) {
        val text = context.assets.open("exercises.json").bufferedReader().use { it.readText() }
        val file = json.decodeFromString<SeedFile>(text)
        val current = prefs.seedVersion()
        if (current >= file.versao && dao.builtInCount() > 0) return@withContext
        val favorites = dao.favoriteIds().toSet()
        dao.insertAll(file.exercicios.map { it.toEntity(it.id in favorites) })
        val keep = file.exercicios.mapTo(HashSet()) { it.id }
        dao.builtInIds().filterNot { it in keep }.chunked(500).forEach { dao.deleteBuiltIn(it) }
        prefs.setSeedVersion(file.versao)
    }

    private fun SeedExercise.toEntity(favorite: Boolean) = ExerciseEntity(
        id = id,
        name = nome,
        nameEn = nomeEn,
        category = categoria,
        level = nivel,
        equipment = equipamento,
        primaryMuscles = principais,
        secondaryMuscles = secundarios,
        homeFriendly = casa,
        timeBased = porTempo,
        instructions = instrucoes,
        images = imagens,
        tip = dica,
        translated = traduzido,
        favorite = favorite,
    )
}
