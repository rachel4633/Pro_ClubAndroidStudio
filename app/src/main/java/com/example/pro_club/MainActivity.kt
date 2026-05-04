package com.example.pro_club

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.pro_club.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    // MainActivity is now just a shell — it holds the bottom
    // navigation bar and swaps fragments in and out
    // All the schedule logic moved to ScheduleFragment.kt
    // Same as App.tsx in React just handling routes — no business logic

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load Schedule tab first when app opens
        // Same as your default route "/" in React Router
        loadFragment(ScheduleFragment())

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

    private fun loadFragment(fragment: Fragment) {
        // Swaps the current fragment with the new one
        // fragmentContainer is the FrameLayout in activity_main.xml
        // Same as React Router swapping components based on route
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}