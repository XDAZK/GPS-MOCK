package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.MockHistoryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MockHistoryDao {
    @Query("SELECT * FROM mock_history ORDER BY startedAt DESC")
    fun getAllHistory(): Flow<List<MockHistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MockHistoryEntry): Long

    @Query("UPDATE mock_history SET endedAt = :endedAt, durationSeconds = :durationSeconds WHERE id = :id")
    suspend fun updateEnd(id: Long, endedAt: Long, durationSeconds: Long)

    @Delete
    suspend fun delete(entry: MockHistoryEntry)

    @Query("DELETE FROM mock_history")
    suspend fun clearAll()
}
