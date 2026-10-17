package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "route_schedules")
@JsonClass(generateAdapter = true)
data class RouteSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val speedKmh: Float = 15f,
    val intervalSeconds: Int = 2,
    val loopMode: String = "LOOP", // "LOOP", "PING_PONG", "ONCE"
    val scheduleType: String = "CONTINUOUS", // "CONTINUOUS" or "TIMED_HOP"
    val waypointsJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis()
)
