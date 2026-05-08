package com.example.pro_club

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {
    // BroadcastReceiver listens for alarms fired by AlarmManager
    // When the alarm fires at the block's start time
    // this class receives it and shows the notification
    // Same as a scheduled task or cron job in backend development

    override fun onReceive(context: Context, intent: Intent) {
        // Get the block details from the intent
        // These were passed when the alarm was scheduled
        val title = intent.getStringExtra("title") ?: "Time for your next block!"
        val motivation = intent.getStringExtra("motivation") ?: "Stay focused!"
        val blockId = intent.getIntExtra("block_id", 0)
        // Same as receiving data from a scheduled notification
        // in React Native's PushNotification library

        // Create notification channel (required for Android 8+)
        createNotificationChannel(context)

        //check notification before notifying

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = context.checkSelfPermission(
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) return
            // ↑ exit silently if permission not granted — no crash
        }
        //intent to open app when notification is tapped
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            blockId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build and show the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            // Small icon shown in the status bar
            .setContentTitle(title)
            // The bold title of the notification — the block title
            .setContentText(motivation)
            // The body text — the motivation quote
            .setStyle(NotificationCompat.BigTextStyle().bigText(motivation))
            // BigTextStyle shows the full motivation text
            // even if it's long — expands when tapped
            .setPriority(NotificationCompat.PRIORITY_MAX)
            // HIGH priority makes it pop up on screen
            // Same as a heads-up notification on Android
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            //enables default sound + vibration
            .setVibrate(longArrayOf(0, 500, 200, 500))
            //vibration pattern makes it feel urgent
            .setContentIntent(pendingIntent)
            //open app when tapped


            .setAutoCancel(true)
            // Auto dismiss when user taps it
            // Same as dismissing a notification in React Native
            .build()

        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        // Show the notification — blockId makes each unique
        // Using blockId as notification ID means each block
        // has its own notification that can be updated separately
        notificationManager.notify(blockId, notification)
    }

    private fun createNotificationChannel(context: Context) {
        // Notification channels are required on Android 8+
        // They let users control which notifications they receive
        // Same as notification categories in iOS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Schedule Reminders",
                NotificationManager.IMPORTANCE_HIGH
                // IMPORTANCE_HIGH = shows as heads-up popup
            ).apply {
                description = "Notifications for your scheduled blocks"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200,500)
                enableLights(true)
                //blinks notification LED if phone has one

            }
            val manager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "pro_club_schedule_v2"
        // Channel ID must be unique per app
        // Used to identify our notification channel
    }
}