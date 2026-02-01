package org.wspcgir.strong_giraffe.repository

import android.util.Log
import org.wspcgir.strong_giraffe.model.*
import org.wspcgir.strong_giraffe.model.ids.*
import org.wspcgir.strong_giraffe.model.set.MuscleSetHistory
import org.wspcgir.strong_giraffe.model.set.SetContent
import org.wspcgir.strong_giraffe.model.set.SetSummary
import org.wspcgir.strong_giraffe.model.set.SetsForMuscleInWeek
import org.wspcgir.strong_giraffe.model.set.WorkoutSet
import org.wspcgir.strong_giraffe.model.variation.ExerciseVariation
import org.wspcgir.strong_giraffe.model.variation.VariationContent
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*
import org.wspcgir.strong_giraffe.repository.entity.Location as LocationEntity
import org.wspcgir.strong_giraffe.repository.entity.Equipment as EquipmentEntity
import org.wspcgir.strong_giraffe.repository.entity.Muscle as MuscleEntity
import org.wspcgir.strong_giraffe.repository.entity.Exercise as ExerciseEntity
import org.wspcgir.strong_giraffe.repository.entity.set.WorkoutSet as WorkoutSetEntity
import org.wspcgir.strong_giraffe.repository.entity.variation.ExerciseVariation as ExerciseVariationEntity

class AppRepository(private val dao: AppDao) {
    suspend fun newLocation(id: String = UUID.randomUUID().toString()): Location {
        val name = "New Location"
        dao.insertLocation(LocationEntity(id, name))
        return Location(LocationId(id), name)
    }
    suspend fun updateLocation(id: LocationId, newName: String) {
        dao.updateLocation(id.value, newName)
    }
    suspend fun getLocations(): List<Location> {
        val entities = dao.getLocations()
        return entities.map { e -> Location(LocationId(e.id), e.name) }
    }

    suspend fun getEquipment(): List<Equipment> {
        val entities = dao.getEquipment()
        return entities.map { e -> Equipment(EquipmentId(e.id), e.name, LocationId(e.location)) }
    }

    suspend fun newEquipment(location: LocationId): Equipment {
        val id = UUID.randomUUID().toString()
        val name = "New Equipment"
        dao.insertEquipment(EquipmentEntity(id, name, location.value))
        return Equipment(EquipmentId(id), name, location)
    }

    suspend fun updateEquipment(id: EquipmentId, name: String, location: LocationId) {
        dao.updateEquipment(id.value, name, location.value)
    }

    suspend fun getMuscles(): List<Muscle> {
        return dao.getAllMuscles().map { e -> Muscle(MuscleId(e.id), e.name) }
    }

    suspend fun newMuscle(id: String = UUID.randomUUID().toString()): Muscle {
        val name = "New Muscle"
        dao.insertMuscle(MuscleEntity(id, name))
        return Muscle(MuscleId(id), name)
    }

    suspend fun updateMuscle(muscleId: MuscleId, name: String) {
        dao.updateMuscle(muscleId.value, name)
    }

    suspend fun getExercises(): List<Exercise> {
        val entities = dao.getExercises()
        return entities.map { e -> Exercise(ExerciseId(e.id), e.name, MuscleId(e.muscle)) }
    }

    suspend fun getExerciseFromId(id: ExerciseId): Exercise {
        val e = dao.getExercise(id.value)
        return Exercise(ExerciseId(e.id), e.name, MuscleId(e.muscle))
    }

    suspend fun newExercise(
        muscle: MuscleId,
        id: ExerciseId = ExerciseId(UUID.randomUUID().toString()),
    ): Exercise {
        val name = "New Exercise"
        dao.insertExercise(ExerciseEntity(id.value, name, muscle.value))
        return Exercise(ExerciseId(id.value), name, muscle)

    }

    suspend fun updateExercise(id: ExerciseId, name: String, muscle: MuscleId) {
        dao.updateExercise(id.value, name, muscle.value)
    }

    suspend fun newWorkoutSet(
        exercise: ExerciseId,
        time: Time = Time(Instant.now()),
        id: SetId = SetId(UUID.randomUUID().toString()),
    ): WorkoutSet {
        val reps = 10
        val comment = ""
        val weight = 0.0f
        dao.insertWorkoutSet(
            WorkoutSetEntity(
                id = id.value,
                exercise = exercise.value,
                location = null,
                equipment = null,
                variation = null,
                reps = reps,
                weight = weight,
                time = time.value.epochSecond,
                intensity = Intensity.NORMAL,
                comment = comment
            )
        )
        return WorkoutSet(
            id = id,
            exercise = exercise,
            location = null,
            equipment = null,
            variation = null,
            reps = Reps(reps),
            weight = Weight(weight),
            time = time,
            intensity = Intensity.Normal,
            comment = Comment(comment),
        )
    }

    suspend fun getSetSummaries(): List<SetSummary> {
        val entities = dao.getSetSummaries()
        return entities.map { e ->
            SetSummary(
                id = SetId(e.id),
                exerciseName = e.exerciseName,
                exerciseId = ExerciseId(e.exerciseId),
                reps = Reps(e.reps),
                weight = Weight(e.weight),
                time = OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(e.time),
                    TimeZone.getDefault().toZoneId()
                ),
                intensity = Intensity.fromInt(e.intensity)!!
            )
        }
    }

    suspend fun updateWorkoutSet(
        original: WorkoutSet,
        exercise: ExerciseId? = null,
        location: LocationId? = null,
        variation: ExerciseVariationId? = null,
        reps: Reps? = null,
        weight: Weight? = null,
        time: Time? = null,
        intensity: Intensity? = null,
        comment: Comment? = null
    ) {
        updateWorkoutSet(
            id = original.id,
            exercise = exercise ?: original.exercise,
            location = location ?: original.location,
            variation = variation ?: original.variation,
            reps = reps ?: original.reps,
            weight = weight ?: original.weight,
            time = time ?: original.time,
            intensity = intensity ?: original.intensity,
            comment = comment ?: original.comment,
        )
    }

    suspend fun updateWorkoutSet(
        id: SetId,
        exercise: ExerciseId,
        variation: ExerciseVariationId?,
        reps: Reps,
        weight: Weight,
        time: Time,
        intensity: Intensity,
        comment: Comment,
        location: LocationId? = null,
    ) {
        dao.updateWorkoutSet(
            id = id.value,
            exercise = exercise.value,
            location = location?.value,
            variation = variation?.value,
            reps = reps.value,
            weight = weight.value,
            time = time.value.epochSecond,
            intensity = Intensity.toInt(intensity),
            comment = comment.value,
        )
    }

    suspend fun getSetFromId(id: SetId): SetContent {
        val e = dao.getWorkoutSetContent(id.value)
        return SetContent(
            id = SetId(e.id),
            exercise = ExerciseId(e.exercise),
            exerciseName = e.exerciseName,
            variation = e.variation?.let { ExerciseVariationId(it) },
            variationName = e.variationName,
            reps = Reps(e.reps),
            weight = Weight(e.weight),
            intensity = Intensity.fromInt(e.intensity)!!,
            time = Time(Instant.ofEpochSecond(e.time)),
            comment = Comment(e.comment)
        )
    }

    suspend fun deleteLocation(id: LocationId) {
        dao.deleteLocation(id.value)
    }

    suspend fun deleteEquipment(id: EquipmentId) {
        dao.deleteEquipment(id.value)
    }

    suspend fun deleteMuscle(id: MuscleId) {
        dao.deleteMuscle(id.value)
    }

    suspend fun deleteExercise(id: ExerciseId) {
        dao.deleteExercise(id.value)
    }

    suspend fun deleteWorkoutSet(id: SetId) {
        dao.deleteWorkoutSet(id.value)
    }

    suspend fun setsForMusclesInWeek(now: Instant): SetsForMuscleInWeek {
        val zone = TimeZone.getDefault()
        val range = WeekRange.forInstant(now, zone)
        val lastWeek = dao
            .setsInWeek(
                range.start.minusWeeks(1).toEpochSecond(),
                range.end.minusWeeks(1).toEpochSecond()
            )
            .fold(emptyMap<MuscleId, Int>()) { map, x ->
                map.plus(MuscleId(x.muscleId) to x.setCount)
            }
        val thisWeek = dao
            .setsInWeek(range.start.toEpochSecond(), range.end.toEpochSecond())
            .fold(emptyMap<MuscleId, MuscleSetHistory>()) { map, x ->
                val id = MuscleId(x.muscleId)
                map.plus(
                    id to MuscleSetHistory(x.muscleName, x.setCount, lastWeek[id] ?: 0)
                )
            }
        return SetsForMuscleInWeek(range, thisWeek)
    }

    private fun workoutSetFromEntity(e: WorkoutSetEntity): WorkoutSet {
        return WorkoutSet(
            id = SetId(e.id),
            exercise = ExerciseId(e.exercise),
            location = e.location?.let { LocationId(it) },
            equipment = e.equipment?.let { EquipmentId(it) },
            variation  = e.variation?.let { ExerciseVariationId(it) },
            reps = Reps(e.reps),
            weight = Weight(e.weight),
            intensity = Intensity.fromInt(e.intensity)!!,
            time = Time(Instant.ofEpochSecond(e.time)),
            comment = Comment(e.comment)
        )
    }

    suspend fun latestSetNot(set: SetId): WorkoutSet? {
        val e = dao.getLatestWorkoutSetNot(set.value)
        return if (e != null) { workoutSetFromEntity(e) } else { null }
    }

    suspend fun dropDb() {
        dao.deleteAllLocations()
        dao.deleteAllMuscles()
        dao.deleteAllEquipment()
        dao.deleteAllExercises()
        dao.deleteAllWorkoutSets()
    }

    suspend fun setForExerciseAndVariationBefore(
        cutoff: Time,
        exercise: ExerciseId,
        variation: ExerciseVariationId?,
        limit: Int
    ): List<WorkoutSet> {
        val es = dao.workoutSetsForExerciseWithVariationBefore(
            cutoff.value.epochSecond,
            exercise.value,
            variation?.value,
            limit
        )
        return es.map { e -> workoutSetFromEntity(e) }
    }

    suspend fun getVariationsForExercise(exercise: ExerciseId): List<ExerciseVariation> {
        val es = dao.getVariationsForExercise(exercise.value)
        return es.map { e -> ExerciseVariation(
            ExerciseVariationId(e.id),
            e.name,
            ExerciseId(e.exercise),
            e.location?.let { LocationId(it) }
        )
        }
    }

    suspend fun updateVariation(id: ExerciseVariationId, name: String, location: LocationId?) {
        Log.d("AppRepository.updateVariation", "'$id' '$name' '$location'")
        dao.updateExerciseVariation(id.value, name, location?.value)
    }

    suspend fun newExerciseVariation(
        exercise: ExerciseId,
        id: ExerciseVariationId = ExerciseVariationId(UUID.randomUUID().toString()),
    ): ExerciseVariation {
        val name = "New Variation"
        dao.insertExerciseVariation(
            ExerciseVariationEntity(
                id = id.value,
                name = "New Variation",
                exercise = exercise.value,
                location = null
            )
        )
        return ExerciseVariation(ExerciseVariationId(id.value), name, exercise, null)
    }

    suspend fun getSets(): List<WorkoutSet> {
        return dao.getSets().map {
            WorkoutSet(
                id = SetId(it.id),
                exercise = ExerciseId(it.exercise),
                variation = it.variation?.let { v -> ExerciseVariationId(v)},
                location = it.location?.let { l -> LocationId(l) },
                comment = Comment(it.comment),
                reps = Reps(it.reps),
                time = Time(Instant.ofEpochSecond(it.time)),
                equipment = it.equipment?.let { e -> EquipmentId(e) },
                weight = Weight(it.weight),
                intensity = Intensity.fromInt(it.intensity)!!
            )
        }
    }

    suspend fun getVariations(): List<ExerciseVariation> {
        return dao.getVariations().map {
            ExerciseVariation(
                id = ExerciseVariationId(it.id),
                exercise = ExerciseId(it.exercise),
                name = it.name,
                location = it.location?.let { l -> LocationId(l) }
            )
        }
    }

    suspend fun getVariationForId(id: ExerciseVariationId): VariationContent {
        return dao.getVariationContentForId(id.value).let {
            VariationContent(
                id = ExerciseVariationId(it.id),
                name = it.name,
                location = it.location?.let { l -> LocationId(l) },
                locationName = it.locationName
            )
        }
    }

    suspend fun createBackup(): Backup {
        return Backup(
            locations = getLocations(),
            muscles = getMuscles(),
            exercises = getExercises(),
            sets = getSets(),
            variations = getVariations()
        )
    }

    suspend fun restoreFromBackup(backup: Backup) {
        dao.insertLocations(backup.locations.map {
            LocationEntity(it.id.value, it.name)
        })
        dao.insertMuscles(backup.muscles.map {
            MuscleEntity(it.id.value, it.name)
        })
        dao.insertExercises(backup.exercises.map {
            ExerciseEntity(it.id.value, it.name, it.muscle.value)
        })
        dao.insertExerciseVariations(backup.variations.map {
            ExerciseVariationEntity(it.id.value, it.name, it.exercise.value, it.location?.value)
        })
        dao.insertWorkoutSets(backup.sets.map {
                WorkoutSetEntity(
                    id = it.id.value,
                    exercise = it.exercise.value,
                    location = it.location?.value,
                    equipment = null,
                    variation = it.variation?.value,
                    reps = it.reps.value,
                    weight = it.weight.value,
                    time = it.time.value.epochSecond,
                    intensity = Intensity.toInt(it.intensity),
                    comment = it.comment.value
                )
        })
    }

}