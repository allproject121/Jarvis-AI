package com.example.execution

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppLauncherService(private val context: Context) {

    suspend fun launchApp(packageName: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return@withContext true
            }

            // Fallback for special shortcuts
            when (packageName) {
                "camera" -> {
                    val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(cameraIntent)
                    return@withContext true
                }
                "settings" -> {
                    val settingsIntent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingsIntent)
                    return@withContext true
                }
            }

            // Fallback to web search or store
            Toast.makeText(context, "App $packageName not installed. Opening details...", Toast.LENGTH_SHORT).show()
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot launch app: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
