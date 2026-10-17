package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SavedLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedLocationDao {
    @Query("SELECT * FROM saved_locations ORDER BY isPreset ASC, createdAt DESC")
    fun getAllSavedLocations(): Flow<List<SavedLocation>>

    @Query("SELECT * FROM saved_locations WHERE category = :category ORDER BY createdAt DESC")
    fun getByCategory(category: String): Flow<List<SavedLocation>>

    @Query("SELECT * FROM saved_locations WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SavedLocation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: SavedLocation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<SavedLocation>)

    @Update
    suspend fun update(location: SavedLocation)

    @Delete
    suspend fun delete(location: SavedLocation)

    @Query("DELETE FROM saved_locations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM saved_locations")
    suspend fun count(): Int
}
