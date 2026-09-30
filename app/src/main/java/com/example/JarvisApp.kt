package com.example

import android.app.Application
import com.example.data.JarvisDatabase
import com.example.data.repository.JarvisRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class JarvisApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { JarvisDatabase.getDatabase(this) }
    val repository by lazy { JarvisRepository(database) }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            repository.seedDefaultsIfEmpty()
        }
    }
}
