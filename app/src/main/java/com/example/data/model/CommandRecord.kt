package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "commands_history")
data class CommandRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "primary_user",
    val originalText: String,
    val parsedIntent: String,
    val extractedEntities: String, // JSON string or description
    val actionsExecuted: String,   // summary of steps executed
    val timestamp: Long = System.currentTimeMillis(),
    val duration: Long = 0L,
    val success: Boolean = true,
    val errorMessage: String? = null,
    val userFeedback: String? = null // CORRECT, INCORRECT, PARTIAL
)
