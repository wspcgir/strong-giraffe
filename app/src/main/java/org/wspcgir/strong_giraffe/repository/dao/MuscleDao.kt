package org.wspcgir.strong_giraffe.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.wspcgir.strong_giraffe.repository.entity.Muscle
import org.wspcgir.strong_giraffe.repository.entity.MuscleSetCount

@Dao
interface MuscleDao {

    @Query(
        """
            SELECT id, name
            FROM muscle
            ORDER BY name
        """
    )
    suspend fun getAllMuscles(): List<Muscle>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMuscles(values: List<Muscle>)

    @Insert
    suspend fun insertMuscle(value: Muscle)

    @Query(
        """
            UPDATE muscle
            SET name = :name
            WHERE id = :id
            
        """
    )
    suspend fun updateMuscle(id: String, name: String)

    @Query(
        """ 
            DELETE from muscle 
            WHERE id = :id
        """
    )
    suspend fun deleteMuscle(id: String)

    @Query(
        """
            DELETE from muscle 
        """
    )
    suspend fun deleteAllMuscles()

    @Query(
        """
            SELECT m.id AS muscle_id
                 , m.name AS muscle_name
                 , SUM(IFNULL(set_counts.count, 0)) AS set_count
            FROM muscle m
              LEFT JOIN (
                SELECT e.muscle AS muscle_id
                     , 1 AS count
                FROM workout_set ws
                  JOIN exercise e ON e.id = ws.exercise
                WHERE :weekStart < ws.time 
                  AND ws.time < :weekEnd 
                  AND 1 < ws.intensity 
                  AND ws.intensity < 4
                ) AS set_counts ON set_counts.muscle_id = m.id
            GROUP BY m.id, m.name
            ORDER BY m.name
        """
    )
    suspend fun setsInWeek(weekStart: Long, weekEnd: Long): List<MuscleSetCount>
}
