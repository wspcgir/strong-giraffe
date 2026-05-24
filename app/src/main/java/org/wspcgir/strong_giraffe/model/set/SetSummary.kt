package org.wspcgir.strong_giraffe.model.set

import org.wspcgir.strong_giraffe.model.Group
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
) {
    companion object {
        fun groupSetsByDateAndExercise(sets: List<SetSummary>): List<Group<Group<SetSummary>>> {
            return Group.fromList(sets.sortedBy { it.time }.asReversed()) {
                it.time
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0)
            }.map { group ->
                group.innerGroup { it.exerciseId.value + (it.variationId?.value ?: "") }
            }
        }
    }
}
