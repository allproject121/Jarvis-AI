package com.example.di

import com.example.data.AppDatabase
import com.example.data.repository.CommandRepository
import com.example.data.repository.EntityRepository
import com.example.data.repository.JarvisRepository
import com.example.data.repository.PatternRepository
import com.example.data.repository.WorkflowGenerationRepository

/**
 * Dependency Injection container interface providing singleton access
 * to the AppDatabase and all data repositories.
 */
interface AppContainer {
    val appDatabase: AppDatabase
    val commandRepository: CommandRepository
    val patternRepository: PatternRepository
    val entityRepository: EntityRepository
    val jarvisRepository: JarvisRepository
    val workflowGenerationRepository: WorkflowGenerationRepository
}
