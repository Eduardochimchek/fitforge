package app.fitforge.data.repo

import app.fitforge.data.db.ExerciseDao
import app.fitforge.data.db.ExerciseEntity
import app.fitforge.domain.model.Exercise
import app.fitforge.domain.model.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepository @Inject constructor(private val dao: ExerciseDao) {
    val all: Flow<List<Exercise>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observe(id: String): Flow<Exercise?> = dao.observe(id).map { it?.toDomain() }

    suspend fun get(id: String): Exercise? = dao.get(id)?.toDomain()

    suspend fun getEntity(id: String): ExerciseEntity? = dao.get(id)

    suspend fun getMany(ids: List<String>): List<Exercise> = dao.getMany(ids).map { it.toDomain() }

    suspend fun setFavorite(id: String, favorite: Boolean) = dao.setFavorite(id, favorite)

    suspend fun saveCustom(entity: ExerciseEntity) = dao.upsert(entity.copy(custom = true, translated = true))

    suspend fun deleteCustom(id: String) = dao.deleteCustom(id)
}
