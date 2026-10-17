package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "saved_locations")
@JsonClass(generateAdapter = true)
data class SavedLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val category: String = "Favorites", // e.g. Favorites, Travel, Gaming, Work, Custom
    val createdAt: Long = System.currentTimeMillis(),
    val isPreset: Boolean = false
)
