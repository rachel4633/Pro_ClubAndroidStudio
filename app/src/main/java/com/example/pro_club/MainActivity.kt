package com.example.pro_club

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.pro_club.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    // MainActivity is now just a shell — it holds the bottom
    // navigation bar and swaps fragments in and out

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🚀 1. Setup Notifications Channel Immediately
        createNotificationChannel()

        // 🚀 2. Load Schedule tab first when app opens
        if (savedInstanceState == null) {
            loadFragment(ScheduleFragment())
        }

        // Listen for bottom nav tab clicks
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_schedule -> loadFragment(ScheduleFragment())
                R.id.nav_weekly   -> loadFragment(WeeklyReviewFragment())
                R.id.nav_profile  -> loadFragment(ProfileFragment())
                R.id.nav_about    -> loadFragment(AboutFragment())
            }
            true
        }
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "pro_club_schedule_v2" // Must match NotificationReceiver
            val channelName = "Schedule Reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Notifications for your scheduled blocks"
            }

            // Get the system service and create the channel
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}