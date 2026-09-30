package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.EntityRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface EntityDao {
    @Query("SELECT * FROM entity_database ORDER BY lastAccessed DESC")
    fun getAllEntities(): Flow<List<EntityRecord>>

    @Query("SELECT * FROM entity_database WHERE entityType = :type")
    fun getEntitiesByType(type: String): Flow<List<EntityRecord>>

    @Query("SELECT * FROM entity_database WHERE LOWER(entityName) = LOWER(:name) OR LOWER(aliases) LIKE '%' || LOWER(:name) || '%' LIMIT 1")
    suspend fun findEntityByName(name: String): EntityRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntity(entity: EntityRecord): Long

    @Update
    suspend fun updateEntity(entity: EntityRecord)

    @Query("UPDATE entity_database SET accessCount = accessCount + 1, lastAccessed = :now WHERE id = :id")
    suspend fun markAccessed(id: Long, now: Long = System.currentTimeMillis())
}
