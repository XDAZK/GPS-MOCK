package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "mock_history")
@JsonClass(generateAdapter = true)
data class MockHistoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mode: String = "fixed", // "fixed" or "route"
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val durationSeconds: Long = 0
)
