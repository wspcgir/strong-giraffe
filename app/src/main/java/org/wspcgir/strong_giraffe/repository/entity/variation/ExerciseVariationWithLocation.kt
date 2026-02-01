package org.wspcgir.strong_giraffe.repository.entity.variation

import androidx.room.DatabaseView

@DatabaseView(
    """
    """
)
data class ExerciseVariationWithLocation(
    val id: String,
    val name: String,
    val exercise: String,
    val location: String?,
    val locationName: String?,
)