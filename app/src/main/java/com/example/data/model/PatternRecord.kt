package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_patterns")
data class PatternRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "primary_user",
    val patternType: String, // DAILY_ROUTINE, WEEKLY, LOCATION_BASED, CONDITIONAL
    val description: String,
    val triggerType: String, // TIME_BASED, LOCATION_BASED, EVENT_BASED, CONDITION_BASED
    val triggerParams: String, // e.g. "09:00 AM", "battery < 20%", "Arrive Office"
    val actions: String,       // JSON or comma separated step list
    val confidenceScore: Float = 0.85f,
    val occurrenceCount: Int = 1,
    val lastOccurrence: Long = System.currentTimeMillis(),
    val enabled: Boolean = true,
    val autoExecute: Boolean = false
)
