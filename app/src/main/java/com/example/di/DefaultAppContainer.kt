package com.example.di

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.repository.CommandRepository
import com.example.data.repository.DefaultCommandRepository
import com.example.data.repository.DefaultEntityRepository
import com.example.data.repository.DefaultPatternRepository
import com.example.data.repository.EntityRepository
import com.example.data.repository.JarvisRepository
import com.example.data.repository.PatternRepository

/**
 * Production implementation of [AppContainer] providing thread-safe,
 * lazy singleton instances of AppDatabase and repositories.
 */
class DefaultAppContainer(private val context: Context) : AppContainer {

    override val appDatabase: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val commandRepository: CommandRepository by lazy {
        DefaultCommandRepository(appDatabase.commandDAO())
    }

    override val patternRepository: PatternRepository by lazy {
        DefaultPatternRepository(appDatabase.patternDAO())
    }

    override val entityRepository: EntityRepository by lazy {
        DefaultEntityRepository(appDatabase.entityDAO())
    }

    override val jarvisRepository: JarvisRepository by lazy {
        JarvisRepository(
            commandRepository = commandRepository,
            patternRepository = patternRepository,
            entityRepository = entityRepository,
            database = appDatabase
        )
    }
}
