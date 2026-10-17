package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RouteSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteScheduleDao {
    @Query("SELECT * FROM route_schedules ORDER BY createdAt DESC")
    fun getAllRouteSchedules(): Flow<List<RouteSchedule>>

    @Query("SELECT * FROM route_schedules WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): RouteSchedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(route: RouteSchedule): Long

    @Update
    suspend fun update(route: RouteSchedule)

    @Delete
    suspend fun delete(route: RouteSchedule)

    @Query("DELETE FROM route_schedules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM route_schedules")
    suspend fun count(): Int
}
