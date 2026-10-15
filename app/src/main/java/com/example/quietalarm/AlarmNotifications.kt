package com.example.quietalarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object AlarmNotifications {
    const val ALARM_CHANNEL = "scheduled_alarm"
    const val MUSIC_CHANNEL = "music_playback"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(NotificationChannel(ALARM_CHANNEL, "Scheduled alarm", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Only appears when a scheduled alarm rings"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            })
            manager.createNotificationChannel(NotificationChannel(MUSIC_CHANNEL, "Music playback", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Playback controls while devotional music is playing"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            })
        }
    }

    fun showAlarm(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val snooze = action(context, AlarmActionReceiver.ACTION_SNOOZE, 11)
        val dismiss = action(context, AlarmActionReceiver.ACTION_DISMISS, 12)
        val stopMusic = action(context, AlarmActionReceiver.ACTION_STOP_MUSIC, 13)
        val continueMusic = action(context, AlarmActionReceiver.ACTION_CONTINUE, 14)
        val builder = if (Build.VERSION.SDK_INT >= 26)
            Notification.Builder(context, ALARM_CHANNEL) else Notification.Builder(context)
        builder.setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Your morning ritual")
            .setContentText("Your scheduled alarm is ringing")
            .setCategory(Notification.CATEGORY_ALARM)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(Notification.Action.Builder(null, "Snooze", snooze).build())
            .addAction(Notification.Action.Builder(null, "Dismiss", dismiss).build())
            .addAction(Notification.Action.Builder(null, "Continue music", continueMusic).build())
            .addAction(Notification.Action.Builder(null, "Stop music", stopMusic).build())
        manager.notify(3001, builder.build())
    }

    private fun action(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, AlarmActionReceiver::class.java).setAction(action)
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
