package org.wspcgir.strong_giraffe.repository.entity

import androidx.room.DatabaseView

@DatabaseView(
    """
    SELECT
      exercise.id AS id,
      exercise.name AS name,
      exercise.muscle AS muscle,
      (archived_exercise.id IS NOT NULL) AS isArchived
    FROM exercise
    LEFT JOIN archived_exercise ON archived_exercise.exercise = exercise.id 
    """
)
data class ExerciseView(
    val id: String,
    val name: String,
    val muscle: String,
    val isArchived: Boolean
)
