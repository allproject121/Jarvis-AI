package com.example.data.repository

import com.example.data.CommandDAO
import com.example.data.model.CommandRecord
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining operations on command history data,
 * abstracting the underlying CommandDAO from the UI and ViewModel layers.
 */
interface CommandRepository {
    val allCommands: Flow<List<CommandRecord>>

    fun getRecentCommands(limit: Int = 15): Flow<List<CommandRecord>>
    suspend fun getRecentCommandsList(limit: Int = 15): List<CommandRecord>
    suspend fun getCommandById(id: Long): CommandRecord?
    fun getCommandByIdFlow(id: Long): Flow<CommandRecord?>
    fun getCommandsByUserId(userId: String): Flow<List<CommandRecord>>
    fun getSuccessfulCommands(): Flow<List<CommandRecord>>
    fun getFailedCommands(): Flow<List<CommandRecord>>
    fun getCommandCount(): Flow<Int>

    suspend fun recordCommand(command: CommandRecord): Long
    suspend fun insertCommands(commands: List<CommandRecord>): List<Long>
    suspend fun updateCommand(command: CommandRecord)
    suspend fun updateFeedback(id: Long, feedback: String)
    suspend fun deleteCommand(command: CommandRecord)
    suspend fun deleteCommandById(id: Long)
    suspend fun clearHistory()
}

/**
 * Default implementation of [CommandRepository] backed by [CommandDAO].
 */
class DefaultCommandRepository(
    private val commandDAO: CommandDAO
) : CommandRepository {

    override val allCommands: Flow<List<CommandRecord>> = commandDAO.getAllCommands()

    override fun getRecentCommands(limit: Int): Flow<List<CommandRecord>> =
        commandDAO.getRecentCommands(limit)

    override suspend fun getRecentCommandsList(limit: Int): List<CommandRecord> =
        commandDAO.getRecentCommandsList(limit)

    override suspend fun getCommandById(id: Long): CommandRecord? =
        commandDAO.getCommandById(id)

    override fun getCommandByIdFlow(id: Long): Flow<CommandRecord?> =
        commandDAO.getCommandByIdFlow(id)

    override fun getCommandsByUserId(userId: String): Flow<List<CommandRecord>> =
        commandDAO.getCommandsByUserId(userId)

    override fun getSuccessfulCommands(): Flow<List<CommandRecord>> =
        commandDAO.getSuccessfulCommands()

    override fun getFailedCommands(): Flow<List<CommandRecord>> =
        commandDAO.getFailedCommands()

    override fun getCommandCount(): Flow<Int> =
        commandDAO.getCommandCount()

    override suspend fun recordCommand(command: CommandRecord): Long =
        commandDAO.insertCommand(command)

    override suspend fun insertCommands(commands: List<CommandRecord>): List<Long> =
        commandDAO.insertCommands(commands)

    override suspend fun updateCommand(command: CommandRecord) =
        commandDAO.updateCommand(command)

    override suspend fun updateFeedback(id: Long, feedback: String) =
        commandDAO.updateFeedback(id, feedback)

    override suspend fun deleteCommand(command: CommandRecord) =
        commandDAO.deleteCommand(command)

    override suspend fun deleteCommandById(id: Long) =
        commandDAO.deleteById(id)

    override suspend fun clearHistory() =
        commandDAO.clearHistory()
}
