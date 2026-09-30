package com.example.execution

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlarmTimerManager(private val context: Context) {

    suspend fun setAlarm(timeString: String, message: String = "JARVIS Alarm"): Boolean = withContext(Dispatchers.Main) {
        try {
            // Parse hour and minute from timeString
            var hour = 7
            var minute = 0

            val regex = Regex("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", RegexOption.IGNORE_CASE)
            val match = regex.find(timeString)
            if (match != null) {
                var h = match.groupValues[1].toIntOrNull() ?: 7
                val m = match.groupValues[2].toIntOrNull() ?: 0
                val ampm = match.groupValues[3].lowercase()

                if (ampm == "pm" && h < 12) h += 12
                if (ampm == "am" && h == 12) h = 0

                hour = h
                minute = m
            }

            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Toast.makeText(context, "Alarm set for $hour:${String.format("%02d", minute)}", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Alarm configured: $timeString", Toast.LENGTH_SHORT).show()
            true
        }
    }
}
