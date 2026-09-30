package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CommandRecord
import com.example.data.model.EntityRecord
import com.example.data.model.PatternRecord

/**
 * Main Room Database class for the application.
 * Manages tables for CommandRecord, PatternRecord, and EntityRecord.
 */
@Database(
    entities = [CommandRecord::class, PatternRecord::class, EntityRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun commandDAO(): CommandDAO
    abstract fun patternDAO(): PatternDAO
    abstract fun entityDAO(): EntityDAO

    // Convenience accessors matching standard naming conventions
    fun commandDao(): CommandDAO = commandDAO()
    fun patternDao(): PatternDAO = patternDAO()
    fun entityDao(): EntityDAO = entityDAO()

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jarvis_automation_db"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
