package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Waypoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val name: String = "",
    val waitSeconds: Int = 0 // Wait time at this waypoint before moving to next
)
