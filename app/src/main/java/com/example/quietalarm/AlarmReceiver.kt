package com.example.quietalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val prefs = context.getSharedPreferences("private_settings", Context.MODE_PRIVATE)
        val uri = prefs.getString("song_uri", null)?.let(Uri::parse)
        AlarmNotifications.showAlarm(context)
        if (uri != null) MusicService.start(context, uri, testOnly = false)
        // Schedule the next daily occurrence. This receiver is private/non-exported.
        if (prefs.getBoolean("alarm_enabled", false)) {
            AlarmScheduler.scheduleNext(context)
        }
    }
}

object AlarmScheduler {
    fun scheduleNext(context: Context) {
        val prefs = context.getSharedPreferences("private_settings", Context.MODE_PRIVATE)
        val hour = prefs.getInt("alarm_hour", 7)
        val minute = prefs.getInt("alarm_minute", 0)
        val next = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        val am = context.getSystemService(android.app.AlarmManager::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) return
        val pi = android.app.PendingIntent.getBroadcast(
            context, 1001, Intent(context, AlarmReceiver::class.java),
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        am.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, next.timeInMillis, pi)
    }
}
