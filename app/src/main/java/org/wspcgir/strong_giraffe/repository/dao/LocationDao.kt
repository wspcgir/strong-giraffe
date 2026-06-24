package org.wspcgir.strong_giraffe.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.wspcgir.strong_giraffe.repository.entity.Location

@Dao
interface LocationDao {

    @Insert
    suspend fun insertLocation(value: Location)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocations(values: List<Location>)

    @Query(
        """
            UPDATE location
            SET name = :newName
            WHERE id = :id
        """
    )
    suspend fun updateLocation(id: String, newName: String)

    @Query(
        """
            SELECT id, name
            FROM location
            ORDER BY name
        """
    )
    suspend fun getLocations(): List<Location>

    @Query(
        """ 
            DELETE from location
            WHERE id = :id
        """
    )
    suspend fun deleteLocation(id: String)

    @Query(
        """
            DELETE from location
        """
    )
    suspend fun deleteAllLocations()
}
