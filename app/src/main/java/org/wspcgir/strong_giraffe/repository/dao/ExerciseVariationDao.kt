package org.wspcgir.strong_giraffe.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.wspcgir.strong_giraffe.repository.entity.variation.ExerciseVariation
import org.wspcgir.strong_giraffe.repository.entity.variation.ExerciseVariationWithLocation
import org.wspcgir.strong_giraffe.repository.entity.variation.VariationContent

@Dao
interface ExerciseVariationDao {

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
        LEFT JOIN location ON exercise_variation.location = location.id
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
}
