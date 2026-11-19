package app.fitforge.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY translated DESC, name COLLATE NOCASE")
    fun observeAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    fun observe(id: String): Flow<ExerciseEntity?>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun get(id: String): ExerciseEntity?

    @Query("SELECT * FROM exercises WHERE id IN (:ids)")
    suspend fun getMany(ids: List<String>): List<ExerciseEntity>

    @Query("SELECT id FROM exercises WHERE favorite = 1")
    suspend fun favoriteIds(): List<String>

    @Query("SELECT COUNT(*) FROM exercises WHERE custom = 0")
    suspend fun builtInCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ExerciseEntity>)

    @Upsert
    suspend fun upsert(item: ExerciseEntity)

    @Query("UPDATE exercises SET favorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean)

    @Query("DELETE FROM exercises WHERE id = :id AND custom = 1")
    suspend fun deleteCustom(id: String)
}
