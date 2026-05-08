package com.example.pro_club

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.pro_club.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animation (fade in + scale)
        binding.logo.alpha = 0f
        binding.logo.scaleX = 0.7f
        binding.logo.scaleY = 0.7f

        binding.logo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(1000)
            .start()

        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val userId = prefs.getString("user_id", "") ?: ""

        Handler(Looper.getMainLooper()).postDelayed({

            if (userId.isNotEmpty()) {
                // User already logged in
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // New user → go to sign in
                startActivity(Intent(this, SignInActivity::class.java))
            }

            finish()

        }, 1800)


    }
}