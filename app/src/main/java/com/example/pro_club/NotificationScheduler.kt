package com.example.pro_club

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object NotificationScheduler {
    // NotificationScheduler is a singleton — one shared instance
    // It schedules alarms for each block using AlarmManager
    // AlarmManager fires at EXACT times even when app is closed
    // Same as a cron job on a server — runs at scheduled times

    fun scheduleNotificationsForBlocks(
        context: Context,
        blocks: List<Block>
    ) {
        // Cancel all existing notifications first
        // Then reschedule fresh — prevents duplicates
        cancelAllNotifications(context, blocks)

        // Check if notifications are enabled in settings
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val notifEnabled = prefs.getBoolean("notifications_enabled", true)

        if (!notifEnabled) return
        // If notifications are off — don't schedule anything
        // Same as checking a toggle state before setting a timer

        for (block in blocks) {
            scheduleBlockNotification(context, block)
        }
    }

    private fun scheduleBlockNotification(context: Context, block: Block) {
        // ✅ CHECK POST_NOTIFICATIONS PERMISSION FIRST
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                android.util.Log.w("NOTIF", "POST_NOTIFICATIONS permission not granted")
                return  // Exit silently — don't crash
            }
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE)
                as AlarmManager
        // AlarmManager is Android's system scheduler
        // Same as setTimeout() but works even when app is closed

        // Build the intent that will be fired when alarm triggers
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", block.title)
            putExtra("motivation", block.motivation)
            putExtra("block_id", block.id)
            // These extras are received by NotificationReceiver.onReceive()
            // Same as passing data with a scheduled notification payload
        }

        // PendingIntent wraps our intent so AlarmManager can fire it
        // even when our app is not running
        // FLAG_UPDATE_CURRENT means update if alarm already exists
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            block.id,
            // Using block.id as request code makes each alarm unique
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            // FLAG_IMMUTABLE is required on Android 12+
        )

        // Calculate the trigger time for today
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, block.startHour)
            set(Calendar.MINUTE, block.startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Same as setting a specific time using new Date() in JavaScript

        // If the time has already passed today, skip it
        if (calendar.timeInMillis <= System.currentTimeMillis()) return
        // System.currentTimeMillis() = Date.now() in JavaScript

        // Schedule the alarm
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ requires checking if exact alarms are allowed
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    // setExactAndAllowWhileIdle fires even in battery saver mode
                    // RTC_WAKEUP = use real clock time and wake device if sleeping
                } else {
                    // ✅ FALLBACK: Use inexact alarm if exact alarms not allowed
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    android.util.Log.w("NOTIF", "canScheduleExactAlarms=false, using fallback")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
<<<<<<< HEAD
            android.util.Log.e("NOTIF", "Failed to schedule: ${block.title}: ${e.message}", e)
=======
            // ✅ BETTER ERROR HANDLING
            android.util.Log.e("NOTIF", "SecurityException scheduling ${block.title}: ${e.message}", e)
>>>>>>> 636b84c47cebeb8a07eebb8b869ddb1bd1e21be3
            // Try fallback
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
<<<<<<< HEAD
            } catch (fallbackE: Exception){
                android.util.Log.e("NOTIF", "Fallback also failed: ${fallbackE.message}", fallbackE)
            }
        }catch (e: Exception){
=======
            } catch (fallbackE: Exception) {
                android.util.Log.e("NOTIF", "Fallback also failed: ${fallbackE.message}", fallbackE)
            }
        } catch (e: Exception) {
>>>>>>> 636b84c47cebeb8a07eebb8b869ddb1bd1e21be3
            android.util.Log.e("NOTIF", "Failed to schedule ${block.title}: ${e.message}", e)
        }
    }

    private fun cancelAllNotifications(context: Context, blocks: List<Block>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE)
                as AlarmManager

        for (block in blocks) {
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                block.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            // Cancel existing alarm for this block
            // Same as clearTimeout() in JavaScript
        }
    }
}
