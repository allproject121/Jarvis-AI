package com.example.di

import android.content.Context
import com.example.JarvisApp

/**
 * Manual Dependency Provider / Service Locator for retrieving singleton dependencies.
 */
object AppDependencyProvider {

    @Volatile
    private var testContainer: AppContainer? = null

    /**
     * Resolves the [AppContainer] singleton, preferring the application-scoped container
     * or initializing a fallback container with the provided context.
     */
    fun getContainer(context: Context): AppContainer {
        testContainer?.let { return it }
        val appContext = context.applicationContext
        return if (appContext is JarvisApp) {
            appContext.container
        } else {
            DefaultAppContainer(appContext)
        }
    }

    /**
     * Allows setting a custom container (e.g. for testing with mocks or in-memory DB).
     */
    fun setContainerForTesting(container: AppContainer?) {
        testContainer = container
    }
}
