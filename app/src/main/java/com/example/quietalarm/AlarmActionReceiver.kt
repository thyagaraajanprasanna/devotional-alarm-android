package com.example.quietalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast

class AlarmActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            ACTION_SNOOZE -> {
                val am = context.getSystemService(AlarmManager::class.java)
                val pi = PendingIntent.getBroadcast(context, 1002, Intent(context, SnoozeReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10 * 60 * 1000, pi)
                }
                context.getSystemService(android.app.NotificationManager::class.java).cancel(3001)
            }
            ACTION_DISMISS -> context.getSystemService(android.app.NotificationManager::class.java).cancel(3001)
            ACTION_STOP_MUSIC -> {
                context.stopService(Intent(context, MusicService::class.java))
                context.getSystemService(android.app.NotificationManager::class.java).cancel(3001)
            }
            ACTION_CONTINUE -> {
                val uri = context.getSharedPreferences("private_settings", Context.MODE_PRIVATE).getString("song_uri", null)?.let(Uri::parse)
                if (uri != null) MusicService.start(context, uri, testOnly = true)
            }
        }
    }
    companion object {
        const val ACTION_SNOOZE = "com.example.quietalarm.SNOOZE"
        const val ACTION_DISMISS = "com.example.quietalarm.DISMISS"
        const val ACTION_STOP_MUSIC = "com.example.quietalarm.STOP_MUSIC"
        const val ACTION_CONTINUE = "com.example.quietalarm.CONTINUE"
    }
}

class SnoozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val uri = context.getSharedPreferences("private_settings", Context.MODE_PRIVATE).getString("song_uri", null)?.let(Uri::parse)
        AlarmNotifications.showAlarm(context)
        if (uri != null) MusicService.start(context, uri, testOnly = false)
    }
}
