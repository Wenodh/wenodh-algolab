package com.wenodh.zen.schedule

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.wenodh.zen.MainActivity

class FocusScheduleWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val schedule = FocusScheduleStore(applicationContext)
        if (!schedule.isEnabled()) return Result.success()
        runCatching { showReminder() }
        schedule.scheduleNext(fromWorker = true)
        return Result.success()
    }

    private fun showReminder() {
        val channelId = "focus_schedule"
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.getNotificationChannel(channelId) == null) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "Focus reminders", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val openApp = PendingIntent.getActivity(
            applicationContext,
            5201,
            Intent(applicationContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        manager.notify(
            NOTIFICATION_ID,
            NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Time for a focus session")
                .setContentText("Your scheduled focus reminder is here.")
                .setContentIntent(openApp)
                .setAutoCancel(true)
                .build()
        )
    }

    companion object { private const val NOTIFICATION_ID = 5201 }
}
