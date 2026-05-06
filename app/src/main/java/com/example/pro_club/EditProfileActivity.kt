package com.example.pro_club

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.pro_club.databinding.ActivityEditProfileBinding
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EditProfileActivity : AppCompatActivity() {
    // EditProfileActivity lets users update their profile details
    // It requires password confirmation before saving any changes
    // Same as a settings page in any real app

    private lateinit var binding: ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load current profile data from SharedPreferences
        // Pre-fill the form so user sees their current values
        // Same as defaultValue in a React controlled input
        loadCurrentProfile()

        // CANCEL BUTTON
        binding.btnCancel.setOnClickListener {
            finish()
            // finish() closes this screen and goes back to Profile tab
        }

        // SAVE BUTTON
        binding.btnSave.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val github = binding.etGithub.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Validate — all fields except github are required
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showError("Username, email and password are required")
                return@setOnClickListener
            }

            showLoading(true)
            updateProfile(username, email, phone, github, password)
        }
    }

    private fun loadCurrentProfile() {
        // Read current values from SharedPreferences
        // These were saved when user logged in
        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)

        binding.etUsername.setText(prefs.getString("username", ""))
        binding.etEmail.setText(prefs.getString("email", ""))
        binding.etPhone.setText(prefs.getString("phone", ""))
        binding.etGithub.setText(prefs.getString("github", ""))
        // Password field is left empty for security
        // User must type their password to confirm changes
    }

    private fun updateProfile(
        username: String,
        email: String,
        phone: String,
        github: String,
        password: String
    ) {
        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = prefs.getString("user_id", "0") ?: "0"

        val retrofit = Retrofit.Builder()
            .baseUrl("https://godchild.alwaysdata.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        service.updateProfile(userId, username, email, phone, github, password)
            .enqueue(object : Callback<ResponseBody> {

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        val rawJson = response.body()?.string()
                        val json = JSONObject(rawJson ?: "{}")
                        val status = json.optString("status")

                        if (status == "success") {
                            // Update SharedPreferences with new values
                            // So ProfileFragment shows the updated data immediately
                            // Same as updating localStorage after a profile update in React
                            prefs.edit()
                                .putString("username", username)
                                .putString("email", email)
                                .putString("phone", phone)
                                .putString("github", github)
                                .apply()

                            // Go back to profile screen
                            finish()
                        } else {
                            // Wrong password or other error
                            showError(json.optString("message", "Update failed"))
                        }
                    } else {
                        showError("Server error — please try again")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    showLoading(false)
                    showError("Network error: " + t.message)
                }
            })
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }
}