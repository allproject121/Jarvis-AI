package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CommandRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandDao {
    @Query("SELECT * FROM commands_history ORDER BY timestamp DESC")
    fun getAllCommands(): Flow<List<CommandRecord>>

    @Query("SELECT * FROM commands_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentCommands(limit: Int = 10): Flow<List<CommandRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommand(command: CommandRecord): Long

    @Update
    suspend fun updateCommand(command: CommandRecord)

    @Query("UPDATE commands_history SET userFeedback = :feedback WHERE id = :id")
    suspend fun updateFeedback(id: Long, feedback: String)

    @Query("DELETE FROM commands_history")
    suspend fun clearHistory()
}
