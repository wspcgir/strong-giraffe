package org.wspcgir.strong_giraffe.repository

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.wspcgir.strong_giraffe.repository.entity.*
import org.wspcgir.strong_giraffe.repository.entity.set.SetContent
import org.wspcgir.strong_giraffe.repository.entity.set.SetSummary
import org.wspcgir.strong_giraffe.repository.entity.set.WorkoutSet
import org.wspcgir.strong_giraffe.repository.entity.variation.ExerciseVariation
import org.wspcgir.strong_giraffe.repository.entity.variation.ExerciseVariationWithLocation
import org.wspcgir.strong_giraffe.repository.entity.variation.VariationContent

@androidx.room.Dao
interface AppDao {

    @Insert
    suspend fun insertLocation(value: Location)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocations(values: List<Location>)

    @Query(
        """
            UPDATE location
            SET name = :newName
            WHERE id = :id
        """
    )
    suspend fun updateLocation(id: String, newName: String)

    @Query(
        """
            SELECT id, name
            FROM location
            ORDER BY name
        """
    )
    suspend fun getLocations(): List<Location>

    @Query(
        """
            SELECT id, name
            FROM muscle
            ORDER BY name
        """
    )
    suspend fun getAllMuscles(): List<Muscle>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMuscles(values: List<Muscle>)

    @Query(
        """
            SELECT id
                 , name
                 , location
            FROM equipment
            ORDER BY location, name
        """
    )
    suspend fun getEquipment(): List<Equipment>

    @Insert
    suspend fun insertEquipment(equipmentEntity: Equipment)

    @Query(
        """
            UPDATE equipment
            SET name = :name
              , location = :location
            WHERE id = :id
        """
    )
    suspend fun updateEquipment(id: String, name: String, location: String)

    @Insert
    suspend fun insertMuscle(value: Muscle)

    @Query(
        """
            UPDATE muscle
            SET name = :name
            WHERE id = :id
            
        """
    )
    suspend fun updateMuscle(id: String, name: String)

    @Query(
        """
            SELECT id
                 , name
                 , muscle
            FROM exercise
            ORDER BY name
        """
    )
    suspend fun getExercises(): List<Exercise>

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
            WHERE time < :cutoff
              AND exercise  = :exercise
              AND variation = :variation
            ORDER BY time DESC
            LIMIT :limit
        """
    )
    suspend fun workoutSetsForExerciseWithVariationBefore(
        cutoff: Long,
        exercise: String,
        variation: String?,
        limit: Int
    ): List<WorkoutSet>

    @Query(
        """ 
            DELETE from location
            WHERE id = :id
        """
    )
    suspend fun deleteLocation(id: String)

    @Query(
        """ 
            DELETE from equipment
            WHERE id = :id
        """
    )
    suspend fun deleteEquipment(id: String)

    @Query(
        """ 
            DELETE from muscle 
            WHERE id = :id
        """
    )
    suspend fun deleteMuscle(id: String)

    @Query(
        """ 
            DELETE from exercise 
            WHERE id = :id
        """
    )
    suspend fun deleteExercise(id: String)

    @Query(
        """ 
            DELETE from workout_set 
            WHERE id = :id
        """
    )
    suspend fun deleteWorkoutSet(id: String)

    @Query(
        """
            SELECT m.id AS muscle_id
                 , m.name AS muscle_name
                 , SUM(IFNULL(set_counts.count, 0)) AS set_count
            FROM muscle m
              LEFT JOIN (
                SELECT e.muscle AS muscle_id
                     , 1 AS count
                FROM workout_set ws
                  JOIN exercise e ON e.id = ws.exercise
                WHERE :weekStart < ws.time 
                  AND ws.time < :weekEnd 
                  AND 1 < ws.intensity 
                  AND ws.intensity < 4
                ) AS set_counts ON set_counts.muscle_id = m.id
            GROUP BY m.id, m.name
            ORDER BY m.name
        """
    )
    suspend fun setsInWeek(weekStart: Long, weekEnd: Long): List<MuscleSetCount>

    @Query(
        """
            DELETE from location
        """
    )
    suspend fun deleteAllLocations()

    @Query(
        """
            DELETE from muscle 
        """
    )
    suspend fun deleteAllMuscles()

    @Query(
        """
            DELETE from equipment 
        """
    )
    suspend fun deleteAllEquipment()

    @Query(
        """
            DELETE from exercise 
        """
    )
    suspend fun deleteAllExercises()

    @Query(
        """
            DELETE from workout_set 
        """
    )
    suspend fun deleteAllWorkoutSets()

    @Query(
        """
            SELECT 
              id,
              name,
              exercise,
              location
            FROM exercise_variation
            WHERE exercise = :exerciseId
        """
    )
    suspend fun getVariationsForExercise(exerciseId: String): List<ExerciseVariation>

    @Query("""
      SELECT exercise_variation.id as id 
           , exercise_variation.name as name 
           , exercise_variation.exercise as exercise
           , location.id as location
           , location.name as locationName
      FROM exercise_variation 
        JOIN location ON exercise_variation.location = location.id
      WHERE exercise = :exerciseId
      """
    )
    suspend fun getVariationsForExerciseWithLocation(exerciseId: String): List<ExerciseVariationWithLocation>

    @Query(
        """
            SELECT 
              exercise_variation.id,
              exercise_variation.name,
              exercise_variation.location,
              location.name AS "locationName"
            FROM exercise_variation
            LEFT JOIN location ON location.id = exercise_variation.location
            WHERE exercise_variation.id = :variationId
        """
    )
    suspend fun getVariationContentForId(variationId: String): VariationContent

    @Query(
        """
            SELECT id
                 , name
                 , exercise 
                 , location 
            FROM exercise_variation
        """
    )
    suspend fun getVariations(): List<ExerciseVariation>

    @Query(
        """
            UPDATE exercise_variation
            SET name = :name,
                location = :location
            WHERE id = :id
        """
    )
    suspend fun updateExerciseVariation(id: String, name: String, location: String?)

    @Insert
    suspend fun insertExerciseVariation(value: ExerciseVariation)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExerciseVariations(values: List<ExerciseVariation>)

    @Query(
        """
        SELECT id,
               name,
               muscle
        FROM Exercise
        WHERE id = :id
        """
    )
    suspend fun getExercise(id: String): Exercise
}