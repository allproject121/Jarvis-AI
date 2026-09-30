package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.repository.CommandRepository
import com.example.data.repository.EntityRepository
import com.example.data.repository.JarvisRepository
import com.example.data.repository.PatternRepository
import com.example.di.AppContainer
import com.example.di.DefaultAppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class JarvisApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var container: AppContainer
        private set

    val database: AppDatabase get() = container.appDatabase
    val repository: JarvisRepository get() = container.jarvisRepository
    val commandRepository: CommandRepository get() = container.commandRepository
    val patternRepository: PatternRepository get() = container.patternRepository
    val entityRepository: EntityRepository get() = container.entityRepository

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        applicationScope.launch {
            repository.seedDefaultsIfEmpty()
        }
    }
}
