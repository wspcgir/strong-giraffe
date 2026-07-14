package org.wspcgir.strong_giraffe.repository.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.wspcgir.strong_giraffe.repository.entity.Exercise
import org.wspcgir.strong_giraffe.repository.entity.archived.ArchivedExercise

@Dao
interface ArchivedExercise {
    @Query(
        """
            SELECT exercise.id,
                   exercise.name,
                   exercise.muscle
            FROM exercise
            LEFT JOIN archived_exercise ON archived_exercise.exercise = exercise.id
            WHERE archived_exercise.id IS NOT NULL
        """
    )
    suspend fun getArchivedExercise(): List<Exercise>

    @Query(
        """
            SELECT 
              exercise.id,
              exercise.name,
              exercise.muscle
            FROM exercise
            LEFT JOIN archived_exercise ON archived_exercise.exercise = exercise.id
            WHERE archived_exercise.id IS NULL
        """
    )
    suspend fun getActiveExercises(): List<Exercise>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun archiveExercise(values: ArchivedExercise)

    @Query(
        """
            DELETE 
            FROM archived_exercise
            WHERE archived_exercise.exercise = :exerciseId
        """
    )
    suspend fun restoreExercise(exerciseId: String)
}