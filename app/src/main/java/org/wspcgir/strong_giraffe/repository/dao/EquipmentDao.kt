package org.wspcgir.strong_giraffe.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import org.wspcgir.strong_giraffe.repository.entity.Equipment

@Dao
interface EquipmentDao {

    @Query(
        """
            SELECT id
                 , name
                 , location
            FROM equipment
            ORDER BY location, name
        """
    )
    suspend fun getEquipment(): List<Equipment>

    @Insert
    suspend fun insertEquipment(equipmentEntity: Equipment)

    @Query(
        """
            UPDATE equipment
            SET name = :name
              , location = :location
            WHERE id = :id
        """
    )
    suspend fun updateEquipment(id: String, name: String, location: String)

    @Query(
        """ 
            DELETE from equipment
            WHERE id = :id
        """
    )
    suspend fun deleteEquipment(id: String)

    @Query(
        """
            DELETE from equipment 
        """
    )
    suspend fun deleteAllEquipment()
}
