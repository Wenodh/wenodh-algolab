package com.wenodh.zen.focus

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FocusSessionService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var ticker: Job? = null
    private lateinit var store: FocusSessionStore

    override fun onCreate() {
        super.onCreate()
        store = FocusSessionStore(this)
        AmbientSoundEngine.initialize(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                store.interrupt()
                AmbientSoundEngine.stop()
                FocusDndController(this).restoreAfterSession()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_AMBIENT_STOP -> {
                AmbientSoundEngine.stop()
                if (store.isActive()) updateForegroundTypes(includeMediaPlayback = false)
                return START_STICKY
            }
            ACTION_AMBIENT_START -> {
                if (store.isActive()) {
                    val selected = AmbientSoundStore(this).selected()
                    if (selected != null) {
                        updateForegroundTypes(includeMediaPlayback = true)
                        AmbientSoundEngine.start(selected)
                    }
                }
                return START_STICKY
            }
        }

        // A sticky restart or reboot can arrive after the persisted deadline.
        if (!store.isActive()) {
            if (store.finishIfExpired()) showCompletionNotification()
            stopSelf()
            return START_NOT_STICKY
        }

        val skipAmbientOnRestart = intent?.getBooleanExtra(EXTRA_SKIP_AMBIENT, false) ?: true
        val selectedAmbient = AmbientSoundStore(this).selected()
        updateForegroundTypes(includeMediaPlayback = !skipAmbientOnRestart && selectedAmbient != null)

        FocusDndController(this).applyForSession()
        if (!skipAmbientOnRestart) selectedAmbient?.let(AmbientSoundEngine::start)
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive && store.isActive()) {
                getSystemService(NotificationManager::class.java)
                    .notify(NOTIFICATION_ID, buildNotification(store.remainingMillis()))
                delay(NOTIFICATION_REFRESH_MS)
            }
            if (store.finishIfExpired()) showCompletionNotification()
            AmbientSoundEngine.stop()
            FocusDndController(this@FocusSessionService).restoreAfterSession()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return START_STICKY
    }

    private fun updateForegroundTypes(includeMediaPlayback: Boolean) {
        val notification = buildNotification(store.remainingMillis())
        if (Build.VERSION.SDK_INT >= 34) {
            var serviceTypes = android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            if (includeMediaPlayback) {
                serviceTypes = serviceTypes or android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            }
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, serviceTypes)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(remainingMillis: Long): Notification {
        val totalSeconds = (remainingMillis / 1000L).coerceAtLeast(0L)
        val time = "%02d:%02d".format(totalSeconds / 60L, totalSeconds % 60L)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Wenodh Zen · Focus active")
            .setContentText("$time remaining")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun showCompletionNotification() {
        runCatching {
            getSystemService(NotificationManager::class.java).notify(
                COMPLETION_NOTIFICATION_ID,
                NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setContentTitle("Focus session complete")
                    .setContentText("Nice work. Your focus session is complete.")
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build()
            )
        }.onFailure { Log.w(TAG, "Completion notification could not be shown", it) }
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Focus session", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        ticker?.cancel()
        AmbientSoundEngine.stop()
        FocusDndController(this).restoreAfterSession()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "FocusSessionService"
        private const val CHANNEL_ID = "focus_session"
        private const val NOTIFICATION_ID = 4101
        private const val COMPLETION_NOTIFICATION_ID = 4102
        private const val NOTIFICATION_REFRESH_MS = 15_000L
        const val ACTION_STOP = "com.wenodh.zen.action.STOP_FOCUS"
        const val ACTION_AMBIENT_START = "com.wenodh.zen.action.AMBIENT_START"
        const val ACTION_AMBIENT_STOP = "com.wenodh.zen.action.AMBIENT_STOP"
        private const val EXTRA_SKIP_AMBIENT = "skip_ambient_on_restart"

        fun start(context: Context, skipAmbient: Boolean = false) {
            val intent = Intent(context, FocusSessionService::class.java)
                .putExtra(EXTRA_SKIP_AMBIENT, skipAmbient)
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.startService(Intent(context, FocusSessionService::class.java).setAction(ACTION_STOP))
        }

        fun setAmbientSound(context: Context, sound: AmbientSoundEngine.Sound?) {
            AmbientSoundStore(context).select(sound)
            if (!FocusSessionStore(context).isActive()) return
            val action = if (sound == null) ACTION_AMBIENT_STOP else ACTION_AMBIENT_START
            context.startService(Intent(context, FocusSessionService::class.java).setAction(action))
        }
    }
}
