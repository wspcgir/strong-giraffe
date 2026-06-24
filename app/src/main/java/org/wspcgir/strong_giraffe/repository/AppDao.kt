package org.wspcgir.strong_giraffe.repository

import androidx.room.Dao
import org.wspcgir.strong_giraffe.repository.dao.EquipmentDao
import org.wspcgir.strong_giraffe.repository.dao.ExerciseDao
import org.wspcgir.strong_giraffe.repository.dao.ExerciseVariationDao
import org.wspcgir.strong_giraffe.repository.dao.LocationDao
import org.wspcgir.strong_giraffe.repository.dao.MuscleDao
import org.wspcgir.strong_giraffe.repository.dao.WorkoutSetDao

@Dao
interface AppDao : LocationDao, MuscleDao, EquipmentDao, ExerciseDao, WorkoutSetDao,
    ExerciseVariationDao
