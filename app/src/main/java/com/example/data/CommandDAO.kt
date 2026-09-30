package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CommandRecord
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) interface for CommandRecord entities.
 * Supports CRUD operations, history queries, and user feedback tracking.
 */
@Dao
interface CommandDAO {

    @Query("SELECT * FROM commands_history ORDER BY timestamp DESC")
    fun getAllCommands(): Flow<List<CommandRecord>>

    @Query("SELECT * FROM commands_history ORDER BY timestamp DESC")
    suspend fun getAllCommandsList(): List<CommandRecord>

    @Query("SELECT * FROM commands_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentCommands(limit: Int = 10): Flow<List<CommandRecord>>

    @Query("SELECT * FROM commands_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentCommandsList(limit: Int = 10): List<CommandRecord>

    @Query("SELECT * FROM commands_history WHERE id = :id")
    suspend fun getCommandById(id: Long): CommandRecord?

    @Query("SELECT * FROM commands_history WHERE id = :id")
    fun getCommandByIdFlow(id: Long): Flow<CommandRecord?>

    @Query("SELECT * FROM commands_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getCommandsByUserId(userId: String): Flow<List<CommandRecord>>

    @Query("SELECT * FROM commands_history WHERE success = 1 ORDER BY timestamp DESC")
    fun getSuccessfulCommands(): Flow<List<CommandRecord>>

    @Query("SELECT * FROM commands_history WHERE success = 0 ORDER BY timestamp DESC")
    fun getFailedCommands(): Flow<List<CommandRecord>>

    @Query("SELECT COUNT(*) FROM commands_history")
    fun getCommandCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM commands_history")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommand(command: CommandRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(command: CommandRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommands(commands: List<CommandRecord>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg commands: CommandRecord): List<Long>

    @Update
    suspend fun updateCommand(command: CommandRecord)

    @Update
    suspend fun update(command: CommandRecord)

    @Delete
    suspend fun deleteCommand(command: CommandRecord)

    @Delete
    suspend fun delete(command: CommandRecord)

    @Query("DELETE FROM commands_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE commands_history SET userFeedback = :feedback WHERE id = :id")
    suspend fun updateFeedback(id: Long, feedback: String)

    @Query("DELETE FROM commands_history")
    suspend fun clearHistory()

    @Query("DELETE FROM commands_history")
    suspend fun deleteAll()
}

typealias CommandDao = CommandDAO
