package org.wspcgir.strong_giraffe.model.set

import org.wspcgir.strong_giraffe.model.Intensity
import org.wspcgir.strong_giraffe.model.Reps
import org.wspcgir.strong_giraffe.model.Weight
import org.wspcgir.strong_giraffe.model.ids.ExerciseId
import org.wspcgir.strong_giraffe.model.ids.ExerciseVariationId
import org.wspcgir.strong_giraffe.model.ids.SetId
import java.time.OffsetDateTime

data class SetSummary(
    val id: SetId,
    val exerciseName: String,
    val exerciseId: ExerciseId,
    val variationName: String?,
    val variationId: ExerciseVariationId?,
    val reps: Reps,
    val weight: Weight,
    val time: OffsetDateTime,
    val intensity: Intensity,
)
