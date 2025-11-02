package org.wspcgir.strong_giraffe.repository.entity.set

import androidx.room.DatabaseView

@DatabaseView(
    """
        SELECT workout_set.id
             , exercise.name as exerciseName
             , exercise.id as exerciseId
             , exercise_variation.name as variationName
             , exercise_variation.id as variationId
             , workout_set.reps
             , workout_set.weight
             , workout_set.time
             , workout_set.intensity
        FROM workout_set
          JOIN exercise on exercise.id = workout_set.exercise
          JOIN exercise_variation on exercise_variation.id = workout_set.variation
    """
)
data class SetSummary(
    val id: String,
    val exerciseName: String,
    val exerciseId: String,
    var variationName: String?,
    var variationId: String?,
    val reps: Int,
    val weight: Float,
    val time: Long,
    val intensity: Int,
)
