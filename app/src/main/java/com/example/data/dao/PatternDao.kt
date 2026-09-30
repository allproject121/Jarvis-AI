package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PatternRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternDao {
    @Query("SELECT * FROM user_patterns ORDER BY confidenceScore DESC, occurrenceCount DESC")
    fun getAllPatterns(): Flow<List<PatternRecord>>

    @Query("SELECT * FROM user_patterns WHERE enabled = 1")
    fun getEnabledPatterns(): Flow<List<PatternRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPattern(pattern: PatternRecord): Long

    @Update
    suspend fun updatePattern(pattern: PatternRecord)

    @Delete
    suspend fun deletePattern(pattern: PatternRecord)

    @Query("UPDATE user_patterns SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE user_patterns SET autoExecute = :autoExecute WHERE id = :id")
    suspend fun setAutoExecute(id: Long, autoExecute: Boolean)

    @Query("UPDATE user_patterns SET occurrenceCount = occurrenceCount + 1, lastOccurrence = :now WHERE id = :id")
    suspend fun recordOccurrence(id: Long, now: Long = System.currentTimeMillis())
}
