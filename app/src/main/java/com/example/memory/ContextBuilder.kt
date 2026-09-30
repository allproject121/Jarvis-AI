package com.example.memory

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import java.util.Calendar

enum class UserActivity {
    WORKING,
    COMMUTING,
    RELAXING,
    IN_MEETING,
    SLEEPING,
    UNKNOWN
}

data class DeviceState(
    val wifiEnabled: Boolean,
    val bluetoothEnabled: Boolean,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val networkConnected: Boolean
)

data class UserContext(
    val timestamp: Long,
    val hourOfDay: Int,
    val dayOfWeek: String,
    val isWeekend: Boolean,
    val deviceState: DeviceState,
    val detectedActivity: UserActivity
)

class ContextBuilder(private val context: Context) {

    fun getCurrentContext(): UserContext {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeekInt = calendar.get(Calendar.DAY_OF_WEEK)
        val isWeekend = dayOfWeekInt == Calendar.SATURDAY || dayOfWeekInt == Calendar.SUNDAY

        val dayNames = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val dayName = dayNames[(dayOfWeekInt - 1).coerceIn(0, 6)]

        val batteryState = getBatteryState()
        val networkConnected = isNetworkConnected()

        val activity = when {
            hour in 23..24 || hour in 0..6 -> UserActivity.SLEEPING
            !isWeekend && hour in 9..17 -> UserActivity.WORKING
            !isWeekend && (hour in 8..9 || hour in 17..18) -> UserActivity.COMMUTING
            else -> UserActivity.RELAXING
        }

        val deviceState = DeviceState(
            wifiEnabled = true, // System state
            bluetoothEnabled = true,
            batteryLevel = batteryState.first,
            isCharging = batteryState.second,
            networkConnected = networkConnected
        )

        return UserContext(
            timestamp = System.currentTimeMillis(),
            hourOfDay = hour,
            dayOfWeek = dayName,
            isWeekend = isWeekend,
            deviceState = deviceState,
            detectedActivity = activity
        )
    }

    private fun getBatteryState(): Pair<Int, Boolean> {
        return try {
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, ifilter)
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 78
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
            val pct = if (scale > 0) (level * 100 / scale) else 78
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            Pair(pct, isCharging)
        } catch (_: Exception) {
            Pair(82, false)
        }
    }

    private fun isNetworkConnected(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return false
            val cap = cm.getNetworkCapabilities(network) ?: return false
            cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true
        }
    }
}
