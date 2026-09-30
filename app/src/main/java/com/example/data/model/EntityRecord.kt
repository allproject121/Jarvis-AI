package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entity_database")
data class EntityRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "primary_user",
    val entityType: String, // CONTACT, APP, LOCATION, SETTING
    val entityName: String, // "Mom", "Gmail", "Office", "Brightness"
    val aliases: String,    // "Mother, Mummy, Mom"
    val resolvedValue: String, // phone number, package name, coordinates, system key
    val metadata: String = "",
    val confidenceScore: Float = 0.95f,
    val accessCount: Int = 1,
    val lastAccessed: Long = System.currentTimeMillis()
)
