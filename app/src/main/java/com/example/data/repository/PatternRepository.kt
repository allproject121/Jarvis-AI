package com.example.data.repository

import com.example.data.PatternDAO
import com.example.data.model.PatternRecord
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining operations on learned user patterns and routines,
 * abstracting PatternDAO from the UI and ViewModel layers.
 */
interface PatternRepository {
    val allPatterns: Flow<List<PatternRecord>>
    val enabledPatterns: Flow<List<PatternRecord>>
    val autoExecutePatterns: Flow<List<PatternRecord>>

    suspend fun getAllPatternsList(): List<PatternRecord>
    suspend fun getPatternById(id: Long): PatternRecord?
    fun getPatternByIdFlow(id: Long): Flow<PatternRecord?>
    fun getPatternsByType(patternType: String): Flow<List<PatternRecord>>
    fun getPatternsByTriggerType(triggerType: String): Flow<List<PatternRecord>>
    fun getPatternCount(): Flow<Int>

    suspend fun insertPattern(pattern: PatternRecord): Long
    suspend fun insertPatterns(patterns: List<PatternRecord>): List<Long>
    suspend fun updatePattern(pattern: PatternRecord)
    suspend fun setPatternEnabled(id: Long, enabled: Boolean)
    suspend fun setPatternAutoExecute(id: Long, autoExecute: Boolean)
    suspend fun recordOccurrence(id: Long)
    suspend fun updateConfidenceScore(id: Long, score: Float)
    suspend fun deletePattern(pattern: PatternRecord)
    suspend fun deletePatternById(id: Long)
    suspend fun clearAllPatterns()
}

/**
 * Default implementation of [PatternRepository] backed by [PatternDAO].
 */
class DefaultPatternRepository(
    private val patternDAO: PatternDAO
) : PatternRepository {

    override val allPatterns: Flow<List<PatternRecord>> = patternDAO.getAllPatterns()
    override val enabledPatterns: Flow<List<PatternRecord>> = patternDAO.getEnabledPatterns()
    override val autoExecutePatterns: Flow<List<PatternRecord>> = patternDAO.getAutoExecutePatterns()

    override suspend fun getAllPatternsList(): List<PatternRecord> =
        patternDAO.getAllPatternsList()

    override suspend fun getPatternById(id: Long): PatternRecord? =
        patternDAO.getPatternById(id)

    override fun getPatternByIdFlow(id: Long): Flow<PatternRecord?> =
        patternDAO.getPatternByIdFlow(id)

    override fun getPatternsByType(patternType: String): Flow<List<PatternRecord>> =
        patternDAO.getPatternsByType(patternType)

    override fun getPatternsByTriggerType(triggerType: String): Flow<List<PatternRecord>> =
        patternDAO.getPatternsByTriggerType(triggerType)

    override fun getPatternCount(): Flow<Int> =
        patternDAO.getPatternCount()

    override suspend fun insertPattern(pattern: PatternRecord): Long =
        patternDAO.insertPattern(pattern)

    override suspend fun insertPatterns(patterns: List<PatternRecord>): List<Long> =
        patternDAO.insertPatterns(patterns)

    override suspend fun updatePattern(pattern: PatternRecord) =
        patternDAO.updatePattern(pattern)

    override suspend fun setPatternEnabled(id: Long, enabled: Boolean) =
        patternDAO.setEnabled(id, enabled)

    override suspend fun setPatternAutoExecute(id: Long, autoExecute: Boolean) =
        patternDAO.setAutoExecute(id, autoExecute)

    override suspend fun recordOccurrence(id: Long) =
        patternDAO.recordOccurrence(id)

    override suspend fun updateConfidenceScore(id: Long, score: Float) =
        patternDAO.updateConfidenceScore(id, score)

    override suspend fun deletePattern(pattern: PatternRecord) =
        patternDAO.deletePattern(pattern)

    override suspend fun deletePatternById(id: Long) =
        patternDAO.deleteById(id)

    override suspend fun clearAllPatterns() =
        patternDAO.clearAllPatterns()
}
