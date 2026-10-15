package com.example.quietalarm

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("private_settings", Context.MODE_PRIVATE) }
    private var selectedSong: Uri? = null
    private lateinit var songLabel: TextView
    private lateinit var timePicker: TimePicker

    private val songPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                selectedSong = uri
                prefs.edit().putString("song_uri", uri.toString()).apply()
                songLabel.text = "Selected song: ${uri.lastPathSegment ?: "audio file"}"
                Toast.makeText(this, "Song selected", Toast.LENGTH_SHORT).show()
            } catch (_: SecurityException) {
                Toast.makeText(this, "Couldn't retain access to that file. Please choose it again.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AlarmNotifications.createChannels(this)
        selectedSong = prefs.getString("song_uri", null)?.let(Uri::parse)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 48, 40, 32)
        }
        layout.addView(TextView(this).apply {
            text = "Your morning ritual"
            textSize = 26f
        })
        layout.addView(TextView(this).apply {
            text = "Offline by design • No ads • No internet permission"
            textSize = 14f
            setPadding(0, 8, 0, 24)
        })

        timePicker = TimePicker(this).apply { setIs24HourView(false) }
        layout.addView(timePicker)

        songLabel = TextView(this).apply {
            text = if (selectedSong != null) "A song is selected" else "No song selected yet"
            textSize = 15f
            setPadding(0, 20, 0, 12)
        }
        layout.addView(songLabel)

        layout.addView(Button(this).apply {
            text = "Choose devotional song"
            setOnClickListener { songPicker.launch(arrayOf("audio/*")) }
        })

        layout.addView(Button(this).apply {
            text = "Schedule daily alarm"
            setOnClickListener { scheduleAlarm() }
        })

        layout.addView(Button(this).apply {
            text = "Cancel alarm"
            setOnClickListener { cancelAlarm() }
        })

        layout.addView(Button(this).apply {
            text = "Test music now"
            setOnClickListener {
                val uri = prefs.getString("song_uri", null)
                if (uri == null) Toast.makeText(this@MainActivity, "Choose a song first", Toast.LENGTH_SHORT).show()
                else MusicService.start(this@MainActivity, Uri.parse(uri), testOnly = true)
            }
        })
        setContentView(layout)
        requestNotificationPermissionIfNeeded()
    }

    private fun scheduleAlarm() {
        val alarmManager = getSystemService(AlarmManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(this, "Allow exact alarms in Android settings to schedule reliably.", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
            return
        }
        val hour = if (Build.VERSION.SDK_INT >= 23) timePicker.hour else timePicker.currentHour
        val minute = if (Build.VERSION.SDK_INT >= 23) timePicker.minute else timePicker.currentMinute
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        prefs.edit().putInt("alarm_hour", hour).putInt("alarm_minute", minute).putBoolean("alarm_enabled", true).apply()
        val intent = Intent(this, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(this, 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pi)
        Toast.makeText(this, "Alarm scheduled for ${String.format("%02d:%02d", hour, minute)}", Toast.LENGTH_LONG).show()
    }

    private fun cancelAlarm() {
        val alarmManager = getSystemService(AlarmManager::class.java)
        val pi = PendingIntent.getBroadcast(this, 1001, Intent(this, AlarmReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pi)
        prefs.edit().putBoolean("alarm_enabled", false).apply()
        Toast.makeText(this, "Alarm cancelled", Toast.LENGTH_SHORT).show()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2001)
        }
    }
}
