package com.example.pro_club

/**
 * Centralized time utilities to prevent conflicts
 * All files (AddBlockActivity, EditBlockActivity, BootReceiver, NotificationScheduler)
 * should use these functions for consistency
 */
object TimeUtils {

    /**
     * Get section name based on start hour (24-hour format: 0-23)
     * All blocks are stored and processed in 24-hour format
     */
    fun getSectionFromTime(startHour: Int): String {
        return when {
            startHour in 5..10 -> "Morning"       // 5am-10:59am
            startHour in 11..13 -> "Classes"      // 11am-1:59pm
            startHour in 14..16 -> "Afternoon"    // 2pm-4:59pm
            startHour in 17..21 -> "Evening"      // 5pm-9:59pm
            else -> "General"                     // Everything else (midnight-4:59am, 10pm+)
        }
    }

    /**
     * Convert 24-hour format to 12-hour AM/PM display
     * Used for displaying times in the UI
     */
    fun formatTo12Hour(hour: Int, minute: Int): String {
        val period = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12         // midnight = 12 AM
            hour > 12 -> hour - 12  // 13 → 1 PM, 21 → 9 PM
            else -> hour            // 1-12 stays same
        }
        return String.format("%d:%02d %s", displayHour, minute, period)
    }

    /**
     * Format time range for display (e.g., "7:30 AM - 11:00 AM")
     */
    fun formatTimeRange(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ): String {
        return "${formatTo12Hour(startHour, startMinute)} - ${formatTo12Hour(endHour, endMinute)}"
    }
}
