package org.wspcgir.strong_giraffe.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.wspcgir.strong_giraffe.repository.entity.Exercise
import org.wspcgir.strong_giraffe.repository.entity.ExerciseView

@Dao
interface ExerciseDao {

    @Query(
        """
            SELECT id
                 , name
                 , muscle
                 , isArchived
            FROM ExerciseView
            ORDER BY name
        """
    )
    suspend fun getExercises(): List<ExerciseView>

    @Insert
    suspend fun insertExercise(value: Exercise)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercises(values: List<Exercise>)

    @Query(
        """
            UPDATE exercise
            SET name = :name
              , muscle = :muscle
            WHERE id = :id
        """
    )
    suspend fun updateExercise(id: String, name: String, muscle: String)

    @Query(
        """ 
            DELETE from exercise 
            WHERE id = :id
        """
    )
    suspend fun deleteExercise(id: String)

    @Query(
        """
            DELETE from exercise 
        """
    )
    suspend fun deleteAllExercises()

    @Query(
        """
        SELECT id,
               name,
               muscle,
               isArchived
        FROM ExerciseView 
        WHERE id = :id
        """
    )
    suspend fun getExercise(id: String): ExerciseView
}
