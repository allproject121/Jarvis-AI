package com.example.data.repository

import com.example.data.EntityDAO
import com.example.data.model.EntityRecord
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining operations on entities and aliases,
 * abstracting EntityDAO from the UI and ViewModel layers.
 */
interface EntityRepository {
    val allEntities: Flow<List<EntityRecord>>

    suspend fun getAllEntitiesList(): List<EntityRecord>
    fun getEntitiesByType(type: String): Flow<List<EntityRecord>>
    suspend fun findEntityByName(name: String): EntityRecord?
    suspend fun getEntityByName(name: String): EntityRecord?
    suspend fun getEntityById(id: Long): EntityRecord?
    fun getEntityByIdFlow(id: Long): Flow<EntityRecord?>
    fun searchEntities(query: String): Flow<List<EntityRecord>>
    fun getEntityCount(): Flow<Int>

    suspend fun insertEntity(entity: EntityRecord): Long
    suspend fun insertEntities(entities: List<EntityRecord>): List<Long>
    suspend fun updateEntity(entity: EntityRecord)
    suspend fun markEntityAccessed(id: Long, now: Long = System.currentTimeMillis())
    suspend fun deleteEntity(entity: EntityRecord)
    suspend fun deleteEntityById(id: Long)
    suspend fun clearAllEntities()
}

/**
 * Default implementation of [EntityRepository] backed by [EntityDAO].
 */
class DefaultEntityRepository(
    private val entityDAO: EntityDAO
) : EntityRepository {

    override val allEntities: Flow<List<EntityRecord>> = entityDAO.getAllEntities()

    override suspend fun getAllEntitiesList(): List<EntityRecord> =
        entityDAO.getAllEntitiesList()

    override fun getEntitiesByType(type: String): Flow<List<EntityRecord>> =
        entityDAO.getEntitiesByType(type)

    override suspend fun findEntityByName(name: String): EntityRecord? =
        entityDAO.findEntityByName(name)

    override suspend fun getEntityByName(name: String): EntityRecord? =
        entityDAO.getEntityByName(name)

    override suspend fun getEntityById(id: Long): EntityRecord? =
        entityDAO.getEntityById(id)

    override fun getEntityByIdFlow(id: Long): Flow<EntityRecord?> =
        entityDAO.getEntityByIdFlow(id)

    override fun searchEntities(query: String): Flow<List<EntityRecord>> =
        entityDAO.searchEntities(query)

    override fun getEntityCount(): Flow<Int> =
        entityDAO.getEntityCount()

    override suspend fun insertEntity(entity: EntityRecord): Long =
        entityDAO.insertEntity(entity)

    override suspend fun insertEntities(entities: List<EntityRecord>): List<Long> =
        entityDAO.insertEntities(entities)

    override suspend fun updateEntity(entity: EntityRecord) =
        entityDAO.updateEntity(entity)

    override suspend fun markEntityAccessed(id: Long, now: Long) =
        entityDAO.markAccessed(id, now)

    override suspend fun deleteEntity(entity: EntityRecord) =
        entityDAO.deleteEntity(entity)

    override suspend fun deleteEntityById(id: Long) =
        entityDAO.deleteById(id)

    override suspend fun clearAllEntities() =
        entityDAO.clearAllEntities()
}
