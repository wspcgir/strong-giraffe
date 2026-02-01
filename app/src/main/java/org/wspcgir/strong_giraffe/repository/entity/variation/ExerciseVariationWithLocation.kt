package org.wspcgir.strong_giraffe.repository.entity.variation

import androidx.room.DatabaseView

@DatabaseView(
    """
        SELECT exercise_variation.name as variationName
             , exercise_variation.id as variationId
             , location.id as locationId
        FROM exercise_variation 
          JOIN location ON exercise_variation.location = location.id
    """
)
data class ExerciseVariationWithLocation(
    val id: String,
    val name: String,
    val exercise: String,
    val location: String?,
    val locationName: String?,
)