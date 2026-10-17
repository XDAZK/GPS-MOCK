package com.example.data.repository

import com.example.data.dao.RouteScheduleDao
import com.example.data.dao.SavedLocationDao
import com.example.data.model.RouteSchedule
import com.example.data.model.SavedLocation
import com.example.data.model.Waypoint
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow

class GpsMockRepository(
    private val savedLocationDao: SavedLocationDao,
    private val routeScheduleDao: RouteScheduleDao
) {
    private val moshi = Moshi.Builder().build()
    private val waypointListType = Types.newParameterizedType(List::class.java, Waypoint::class.java)
    private val waypointAdapter = moshi.adapter<List<Waypoint>>(waypointListType)

    val allSavedLocations: Flow<List<SavedLocation>> = savedLocationDao.getAllSavedLocations()
    val allRouteSchedules: Flow<List<RouteSchedule>> = routeScheduleDao.getAllRouteSchedules()

    suspend fun saveLocation(location: SavedLocation): Long {
        return savedLocationDao.insert(location)
    }

    suspend fun updateLocation(location: SavedLocation) {
        savedLocationDao.update(location)
    }

    suspend fun deleteLocation(location: SavedLocation) {
        savedLocationDao.delete(location)
    }

    suspend fun deleteLocationById(id: Long) {
        savedLocationDao.deleteById(id)
    }

    suspend fun saveRoute(route: RouteSchedule): Long {
        return routeScheduleDao.insert(route)
    }

    suspend fun updateRoute(route: RouteSchedule) {
        routeScheduleDao.update(route)
    }

    suspend fun deleteRoute(route: RouteSchedule) {
        routeScheduleDao.delete(route)
    }

    suspend fun deleteRouteById(id: Long) {
        routeScheduleDao.deleteById(id)
    }

    suspend fun getRouteById(id: Long): RouteSchedule? {
        return routeScheduleDao.getById(id)
    }

    fun parseWaypoints(json: String): List<Waypoint> {
        return try {
            waypointAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeWaypoints(waypoints: List<Waypoint>): String {
        return try {
            waypointAdapter.toJson(waypoints)
        } catch (e: Exception) {
            "[]"
        }
    }
}
