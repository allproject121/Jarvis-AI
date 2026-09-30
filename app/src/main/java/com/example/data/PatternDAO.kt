package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PatternRecord
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) interface for PatternRecord entities.
 * Supports routine management, automation patterns, confidence scoring, and execution triggers.
 */
@Dao
interface PatternDAO {

    @Query("SELECT * FROM user_patterns ORDER BY confidenceScore DESC, occurrenceCount DESC")
    fun getAllPatterns(): Flow<List<PatternRecord>>

    @Query("SELECT * FROM user_patterns ORDER BY confidenceScore DESC, occurrenceCount DESC")
    suspend fun getAllPatternsList(): List<PatternRecord>

    @Query("SELECT * FROM user_patterns WHERE enabled = 1 ORDER BY confidenceScore DESC")
    fun getEnabledPatterns(): Flow<List<PatternRecord>>

    @Query("SELECT * FROM user_patterns WHERE autoExecute = 1 ORDER BY confidenceScore DESC")
    fun getAutoExecutePatterns(): Flow<List<PatternRecord>>

    @Query("SELECT * FROM user_patterns WHERE id = :id")
    suspend fun getPatternById(id: Long): PatternRecord?

    @Query("SELECT * FROM user_patterns WHERE id = :id")
    fun getPatternByIdFlow(id: Long): Flow<PatternRecord?>

    @Query("SELECT * FROM user_patterns WHERE patternType = :patternType ORDER BY confidenceScore DESC")
    fun getPatternsByType(patternType: String): Flow<List<PatternRecord>>

    @Query("SELECT * FROM user_patterns WHERE triggerType = :triggerType ORDER BY confidenceScore DESC")
    fun getPatternsByTriggerType(triggerType: String): Flow<List<PatternRecord>>

    @Query("SELECT COUNT(*) FROM user_patterns")
    fun getPatternCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_patterns")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPattern(pattern: PatternRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pattern: PatternRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatterns(patterns: List<PatternRecord>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg patterns: PatternRecord): List<Long>

    @Update
    suspend fun updatePattern(pattern: PatternRecord)

    @Update
    suspend fun update(pattern: PatternRecord)

    @Delete
    suspend fun deletePattern(pattern: PatternRecord)

    @Delete
    suspend fun delete(pattern: PatternRecord)

    @Query("DELETE FROM user_patterns WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM user_patterns")
    suspend fun clearAllPatterns()

    @Query("DELETE FROM user_patterns")
    suspend fun deleteAll()

    @Query("UPDATE user_patterns SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE user_patterns SET autoExecute = :autoExecute WHERE id = :id")
    suspend fun setAutoExecute(id: Long, autoExecute: Boolean)

    @Query("UPDATE user_patterns SET occurrenceCount = occurrenceCount + 1, lastOccurrence = :now WHERE id = :id")
    suspend fun recordOccurrence(id: Long, now: Long = System.currentTimeMillis())

    @Query("UPDATE user_patterns SET confidenceScore = :score WHERE id = :id")
    suspend fun updateConfidenceScore(id: Long, score: Float)
}

typealias PatternDao = PatternDAO
