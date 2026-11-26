package com.wenodh.zen.focus

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
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
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            store.interrupt()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        // A sticky restart or reboot can arrive after the persisted deadline.
        if (!store.isActive()) {
            if (store.finishIfExpired()) showCompletionNotification()
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildNotification(store.remainingMillis())
        if (Build.VERSION.SDK_INT >= 34) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        ticker?.cancel()
        ticker = scope.launch {
            while (isActive && store.isActive()) {
                getSystemService(NotificationManager::class.java)
                    .notify(NOTIFICATION_ID, buildNotification(store.remainingMillis()))
                delay(NOTIFICATION_REFRESH_MS)
            }
            if (store.finishIfExpired()) showCompletionNotification()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return START_STICKY
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
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Focus session", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        ticker?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "focus_session"
        private const val NOTIFICATION_ID = 4101
        private const val COMPLETION_NOTIFICATION_ID = 4102
        private const val NOTIFICATION_REFRESH_MS = 15_000L
        const val ACTION_STOP = "com.wenodh.zen.action.STOP_FOCUS"

        fun start(context: Context) {
            val intent = Intent(context, FocusSessionService::class.java)
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.startService(Intent(context, FocusSessionService::class.java).setAction(ACTION_STOP))
        }
    }
}
