package com.example.quietalarm

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder

class MusicService : Service() {
    private var player: MediaPlayer? = null
    private var currentUri: Uri? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AlarmNotifications.createChannels(this)
        val uri = intent?.data
        if (uri != null && (uri != currentUri || player == null)) {
            releasePlayer()
            currentUri = uri
            player = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                setDataSource(this@MusicService, uri)
                isLooping = false
                setOnCompletionListener {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
                prepare()
                start()
            }
        }
        val notification = buildPlaybackNotification()
        if (Build.VERSION.SDK_INT >= 29) startForeground(3002, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        else startForeground(3002, notification)
        return START_NOT_STICKY
    }

    private fun buildPlaybackNotification(): Notification {
        val stopIntent = Intent(this, AlarmActionReceiver::class.java).setAction(AlarmActionReceiver.ACTION_STOP_MUSIC)
        val stopPi = PendingIntent.getBroadcast(this, 13, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(this, AlarmNotifications.MUSIC_CHANNEL) else Notification.Builder(this)
        return builder.setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Morning music")
            .setContentText("Devotional music is playing")
            .setCategory(Notification.CATEGORY_TRANSPORT)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .addAction(Notification.Action.Builder(null, "Stop music", stopPi).build())
            .build()
    }

    private fun releasePlayer() {
        try { player?.stop() } catch (_: Exception) {}
        try { player?.release() } catch (_: Exception) {}
        player = null
    }

    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }

    companion object {
        fun start(context: Context, uri: Uri, testOnly: Boolean) {
            val intent = Intent(context, MusicService::class.java).setData(uri).putExtra("test_only", testOnly)
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent) else context.startService(intent)
        }
    }
}
