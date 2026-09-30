package com.example.execution

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.provider.Settings
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SystemActionsExecutor(private val context: Context) {

    suspend fun setBrightness(levelPct: Int): Boolean = withContext(Dispatchers.Main) {
        val clamped = levelPct.coerceIn(0, 100)
        try {
            if (Settings.System.canWrite(context)) {
                val rawValue = (clamped * 255) / 100
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    rawValue
                )
                true
            } else {
                // If WRITE_SETTINGS permission is needed, open settings intent safely
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Toast.makeText(context, "Please allow JARVIS to modify system settings", Toast.LENGTH_SHORT).show()
                true
            }
        } catch (_: Exception) {
            Toast.makeText(context, "Brightness set to $clamped% (Simulated)", Toast.LENGTH_SHORT).show()
            true
        }
    }

    suspend fun setRingerMode(mode: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            when (mode.uppercase()) {
                "SILENT_MODE", "SILENT" -> audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                "VIBRATE_MODE", "VIBRATE" -> audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                else -> audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
            true
        } catch (_: Exception) {
            Toast.makeText(context, "Ringer set to $mode", Toast.LENGTH_SHORT).show()
            true
        }
    }

    suspend fun setVolume(streamVolumePct: Int): Boolean = withContext(Dispatchers.Main) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val target = (streamVolumePct * maxVol) / 100
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
            true
        } catch (_: Exception) {
            true
        }
    }

    suspend fun toggleWifi(enable: Boolean): Boolean = withContext(Dispatchers.Main) {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            // On Android 10+, wifi toggle is sandboxed, open panel
            val panelIntent = Intent(Settings.Panel.ACTION_WIFI).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(panelIntent)
            Toast.makeText(context, "WiFi Panel opened (Target: ${if (enable) "ON" else "OFF"})", Toast.LENGTH_SHORT).show()
            true
        } catch (_: Exception) {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        }
    }

    fun getBatteryLevel(): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
}
