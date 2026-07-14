package org.wspcgir.strong_giraffe.repository.entity.archived

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import org.wspcgir.strong_giraffe.repository.entity.Exercise

@Entity(
    tableName = "archived_exercise",
    indices = [
        Index(value = arrayOf("id"))
    ],
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("exercise"),
            onDelete = CASCADE
        )
    ]
)
data class ArchivedExercise (
    @PrimaryKey val id: String,
    @ColumnInfo(name = "exercise") val exercise: String
)