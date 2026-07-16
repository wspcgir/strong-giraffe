package org.wspcgir.strong_giraffe.model

import kotlinx.serialization.Serializable
import org.wspcgir.strong_giraffe.model.ids.ExerciseId
import org.wspcgir.strong_giraffe.model.ids.MuscleId

@Serializable
data class Exercise(
    val id: ExerciseId,
    val name: String,
    val muscle: MuscleId,
    val isArchived: Boolean
)
