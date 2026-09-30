package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.EntityRecord
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) interface for EntityRecord entities.
 * Supports entity resolution, alias matching, type filtering, and access telemetry.
 */
@Dao
interface EntityDAO {

    @Query("SELECT * FROM entity_database ORDER BY lastAccessed DESC")
    fun getAllEntities(): Flow<List<EntityRecord>>

    @Query("SELECT * FROM entity_database ORDER BY lastAccessed DESC")
    suspend fun getAllEntitiesList(): List<EntityRecord>

    @Query("SELECT * FROM entity_database WHERE entityType = :type ORDER BY accessCount DESC")
    fun getEntitiesByType(type: String): Flow<List<EntityRecord>>

    @Query("SELECT * FROM entity_database WHERE LOWER(entityName) = LOWER(:name) OR LOWER(aliases) LIKE '%' || LOWER(:name) || '%' LIMIT 1")
    suspend fun findEntityByName(name: String): EntityRecord?

    @Query("SELECT * FROM entity_database WHERE LOWER(entityName) = LOWER(:name) LIMIT 1")
    suspend fun getEntityByName(name: String): EntityRecord?

    @Query("SELECT * FROM entity_database WHERE id = :id")
    suspend fun getEntityById(id: Long): EntityRecord?

    @Query("SELECT * FROM entity_database WHERE id = :id")
    fun getEntityByIdFlow(id: Long): Flow<EntityRecord?>

    @Query("SELECT * FROM entity_database WHERE LOWER(entityName) LIKE '%' || LOWER(:query) || '%' OR LOWER(aliases) LIKE '%' || LOWER(:query) || '%' ORDER BY accessCount DESC")
    fun searchEntities(query: String): Flow<List<EntityRecord>>

    @Query("SELECT COUNT(*) FROM entity_database")
    fun getEntityCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM entity_database")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntity(entity: EntityRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: EntityRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntities(entities: List<EntityRecord>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg entities: EntityRecord): List<Long>

    @Update
    suspend fun updateEntity(entity: EntityRecord)

    @Update
    suspend fun update(entity: EntityRecord)

    @Delete
    suspend fun deleteEntity(entity: EntityRecord)

    @Delete
    suspend fun delete(entity: EntityRecord)

    @Query("DELETE FROM entity_database WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM entity_database")
    suspend fun clearAllEntities()

    @Query("DELETE FROM entity_database")
    suspend fun deleteAll()

    @Query("UPDATE entity_database SET accessCount = accessCount + 1, lastAccessed = :now WHERE id = :id")
    suspend fun markAccessed(id: Long, now: Long = System.currentTimeMillis())

    @Query("UPDATE entity_database SET accessCount = accessCount + 1, lastAccessed = :now WHERE id = :id")
    suspend fun incrementAccessCount(id: Long, now: Long = System.currentTimeMillis())
}

typealias EntityDao = EntityDAO
