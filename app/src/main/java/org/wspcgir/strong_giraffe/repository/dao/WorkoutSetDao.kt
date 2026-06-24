package org.wspcgir.strong_giraffe.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.wspcgir.strong_giraffe.repository.entity.set.SetContent
import org.wspcgir.strong_giraffe.repository.entity.set.SetSummary
import org.wspcgir.strong_giraffe.repository.entity.set.WorkoutSet

@Dao
interface WorkoutSetDao {

    @Insert
    suspend fun insertWorkoutSet(workoutSetEntity: WorkoutSet)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWorkoutSets(values: List<WorkoutSet>)

    @Query(
        """ 
            SELECT id
                 , exercise 
                 , location
                 , variation
                 , reps
                 , weight
                 , time
                 , intensity
                 , comment
            FROM workout_set
        """)
    suspend fun getSets(): List<WorkoutSet>

    @Query("SELECT * FROM SetSummary")
    suspend fun getSetSummaries(): List<SetSummary>

    @Query(
        """
            UPDATE workout_set
            SET exercise = :exercise
              , location = :location
              , variation = :variation
              , reps = :reps
              , weight = :weight
              , time = :time
              , intensity = :intensity
              , comment = :comment
            WHERE id = :id
        """
    )
    suspend fun updateWorkoutSet(
        id: String,
        exercise: String,
        location: String?,
        variation: String?,
        reps: Int,
        weight: Float,
        time: Long,
        intensity: Int,
        comment: String
    )

    @Query(
        """
            SELECT id
                 , exercise
                 , location
                 , variation 
                 , equipment
                 , reps
                 , weight
                 , time
                 , intensity
                 , comment
            FROM workout_set
            WHERE id = :id
            LIMIT 1
        """
    )
    suspend fun getWorkoutSet(id: String): WorkoutSet

    @Query(
        """
            SELECT workout_set.id
                 , workout_set.exercise
                 , exercise.name AS "exerciseName"
                 , workout_set.variation 
                 , exercise_variation.name AS "variationName"
                 , workout_set.reps
                 , workout_set.weight
                 , workout_set.time
                 , workout_set.intensity
                 , workout_set.comment
            FROM workout_set
            JOIN exercise ON exercise.id = workout_set.exercise
            LEFT JOIN exercise_variation ON exercise_variation.id = workout_set.variation
            WHERE workout_set.id = :id
            LIMIT 1
        """
    )
    suspend fun getWorkoutSetContent(id: String): SetContent

    @Query(
        """
            SELECT id
                 , exercise
                 , location
                 , variation 
                 , equipment
                 , reps
                 , weight
                 , time
                 , intensity
                 , comment
            FROM workout_set
            ORDER BY time DESC
            LIMIT 1
        """
    )
    suspend fun getLatestWorkoutSet(): WorkoutSet?

    @Query(
        """
            SELECT id
                 , exercise
                 , location
                 , variation 
                 , equipment
                 , reps
                 , weight
                 , time
                 , intensity
                 , comment
            FROM workout_set
            WHERE id != :id
            ORDER BY time DESC
            LIMIT 1
        """
    )
    suspend fun getLatestWorkoutSetNot(id: String): WorkoutSet?

    @Query(
        """
            SELECT id
                 , exercise
                 , location
                 , variation 
                 , equipment
                 , reps
                 , weight
                 , time
                 , intensity
                 , comment
            FROM workout_set
            WHERE id != :set
              AND location = :location
              AND exercise = :exercise
            ORDER BY time DESC
            LIMIT 1
        """
    )
    suspend fun getLatestWorkoutSetForExerciseAtLocationExcluding(
        set: String,
        location: String,
        exercise: String
    ): WorkoutSet?

    @Query(
        """
            SELECT id
                 , exerciseName
                 , exerciseId
                 , variationName
                 , variationId
                 , reps
                 , weight
                 , time
                 , intensity
            FROM SetSummary 
            WHERE time < :cutoff
              AND exerciseId  = :exercise
              AND (:variation IS NULL AND variationId IS NULL OR variationId = :variation)
            ORDER BY time DESC
            LIMIT :limit
        """
    )
    suspend fun setSummariesForExerciseWithVariationBefore(
        cutoff: Long,
        exercise: String,
        variation: String?,
        limit: Int
    ): List<SetSummary>

    @Query(
        """ 
            DELETE from workout_set 
            WHERE id = :id
        """
    )
    suspend fun deleteWorkoutSet(id: String)

    @Query(
        """
            DELETE from workout_set 
        """
    )
    suspend fun deleteAllWorkoutSets()
}
