package org.wspcgir.strong_giraffe.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.wspcgir.strong_giraffe.model.ids.EquipmentId
import org.wspcgir.strong_giraffe.model.ids.ExerciseId
import org.wspcgir.strong_giraffe.model.ids.ExerciseVariationId
import org.wspcgir.strong_giraffe.model.ids.LocationId
import org.wspcgir.strong_giraffe.model.ids.SetId
import org.wspcgir.strong_giraffe.model.variation.ExerciseVariation as Variation
import org.wspcgir.strong_giraffe.repository.entity.*
import org.wspcgir.strong_giraffe.repository.entity.set.SetSummary
import org.wspcgir.strong_giraffe.repository.entity.set.WorkoutSet
import org.wspcgir.strong_giraffe.repository.entity.variation.ExerciseVariation
import java.util.UUID

@Database(
    entities = [
        WorkoutSet::class, Location::class,
        Exercise::class, Muscle::class,
        Equipment::class, ExerciseVariation::class
    ],
    views = [
        SetSummary::class
    ],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao
}

val MIGRATION_2_3 = object : Migration(2,3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // No migration work needed, only updated SetSummaries
    }
}

val MIGRATION_1_2 = object : Migration(1,2) {
    override fun migrate(database : SupportSQLiteDatabase) {
        // Step 1. Create and populate the new exercise_variation table with indexes
        database.execSQL(
            """
            CREATE TABLE exercise_variation(
              id TEXT NOT NULL,
              name TEXT NOT NULL,
              exercise TEXT NOT NULL,
              location TEXT NULL,
              PRIMARY KEY(id),
              FOREIGN KEY(exercise) REFERENCES Exercise(id) ON DELETE CASCADE,
              FOREIGN KEY(location) REFERENCES Location(id) ON DELETE CASCADE 
            );
            """.trimIndent()
        )
        database.execSQL("CREATE INDEX IF NOT EXISTS index_exercise_variation_id ON exercise_variation(id);")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_exercise_variation_exercise ON exercise_variation(exercise);")

        // Step 2. Make location and equipment optional, add optional variation column

        // Drop indexes on workout_set
        database.execSQL("DROP INDEX index_workout_set_id;");
        database.execSQL("DROP INDEX index_workout_set_location_exercise_equipment;")
        database.execSQL("DROP INDEX index_workout_set_time;")

        // Create a new table with updated columns,
        // sqlite doesn't let us alter existing columns
        //
        // Changes:
        // - location is now NULL
        // - equipment is now NULL
        // - weight is now REAL
        database.execSQL(
            """
            CREATE TABLE workout_set_new(
              id TEXT NOT NULL,
              exercise TEXT NOT NULL,
              location TEXT NULL,
              variation TEXT NULL,
              equipment TEXT NULL,
              reps INTEGER NOT NULL,
              weight REAL NOT NULL,
              time INTEGER NOT NULL,
              intensity INTEGER NOT NULL,
              comment TEXT NOT NULL,
              PRIMARY KEY(id),
              FOREIGN KEY(exercise) REFERENCES Exercise(id) ON DELETE CASCADE,
              FOREIGN KEY(location) REFERENCES Location(id) ON DELETE CASCADE 
              FOREIGN KEY(variation) REFERENCES exercise_variation(id) ON DELETE CASCADE 
            );
            """.trimIndent()
        )

        // Insert old values into new table and drop old
        database.execSQL(
            """
            INSERT INTO workout_set_new 
            SELECT 
              id,
              exercise,
              location,
              NULL AS variation,
              equipment,
              reps,
              weight,
              time,
              intensity,
              comment
            FROM workout_set;
            """.trimIndent())
        database.execSQL("ALTER TABLE workout_set RENAME TO workout_set_old;")
        database.execSQL("ALTER TABLE workout_set_new RENAME TO workout_set;")

        // Create new indexes
        database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_set_id ON workout_set(id);")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_set_exercise ON workout_set(exercise);")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_workout_set_time ON workout_set(time);")


        // Step 3. Manually derive variations from existing equipment
        val cursor = database.query(
            """
            SELECT
              ws.id AS set_id,
              ws.exercise AS exercise, 
              ws.location AS location, 
              e.id AS equip_id,
              e.name AS equip_name 
            FROM workout_set ws
              JOIN Equipment e ON ws.equipment = e.id
            """.trimIndent())
        var derivations: List<ExerciseVariationDerivation> = emptyList()
        while (cursor.moveToNext()){
            derivations = derivations.plus(
                ExerciseVariationDerivation(
                    setId = SetId(cursor.getString(0)),
                    exerciseId = ExerciseId(cursor.getString(1)),
                    locationId = LocationId(cursor.getString(2)),
                    equipId = EquipmentId(cursor.getString(3)),
                    equipName = cursor.getString(4)
                )
            )
        }
        val assignments = deriveExerciseVariation(derivations)
        for (entry in assignments.variations) {
            val variation = entry.value
            database.execSQL(
                """
                INSERT INTO exercise_variation
                VALUES (
                  '${variation.id.value}',
                  '${variation.name}',
                  '${variation.exercise.value}',
                  '${variation.location?.value}'
                )
                """.trimIndent()
            )
        }
        for (entry in assignments.setAssignments) {
            val variation = assignments.variations[entry.value]!!
            database.execSQL(
                """
                UPDATE workout_set
                SET variation = '${variation.id.value}'
                WHERE id = '${entry.key.value}'
                """.trimIndent()
            )
        }
    }
}

data class ExerciseVariationDerivation(
    val setId: SetId,
    val exerciseId: ExerciseId,
    val locationId: LocationId,
    val equipId: EquipmentId,
    val equipName: String,
)

data class ExerciseVariationAssignments(
    val variations: Map<ExerciseVariationId, Variation>,
    val setAssignments: Map<SetId, ExerciseVariationId>
)

fun deriveExerciseVariation(
    items: List<ExerciseVariationDerivation>
): ExerciseVariationAssignments {
    var exerciseAssignments :
            Map<ExerciseId, Map<LocationId, Map<EquipmentId, ExerciseVariationId>>> = emptyMap()
    var setAssignments : Map<SetId, ExerciseVariationId> = emptyMap()
    var variations : Map<ExerciseVariationId, Variation> = emptyMap()
    for (item in items) {

    val locationAssignments = exerciseAssignments[item.exerciseId] ?: emptyMap()
    val equipmentAssignments = locationAssignments[item.locationId] ?: emptyMap()
    val variationId = equipmentAssignments[item.equipId]
        ?: ExerciseVariationId(UUID.randomUUID().toString())
    val variation = Variation(variationId, item.equipName, item.exerciseId, item.locationId)
    variations = variations.plus(variationId to variation)
    setAssignments = setAssignments.plus(item.setId to variationId)
    exerciseAssignments = exerciseAssignments
        .plus(
            item.exerciseId to locationAssignments
                .plus(
                    item.locationId to equipmentAssignments
                        .plus(item.equipId to variationId)
                )
        )
    }

    return ExerciseVariationAssignments(variations, setAssignments)
}
