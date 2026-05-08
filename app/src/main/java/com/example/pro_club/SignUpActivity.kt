package com.example.pro_club

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.pro_club.databinding.ActivitySignUpBinding
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SignUpActivity : AppCompatActivity() {
    // SignUpActivity is the registration screen
    // New users come here to create an account
    // Same as your /signup route in React

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SIGN UP BUTTON CLICK
        binding.btnSignUp.setOnClickListener {
            // Get all field values
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val github = binding.etGithub.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // VALIDATE — check nothing is empty
            if (username.isEmpty() || email.isEmpty() ||
                phone.isEmpty() || password.isEmpty()) {
                showError("Please fill in all required fields")
                return@setOnClickListener
            }

            // Show loading
            showLoading(true)

            // Call the signup API
            signUp(username, email, phone, github, password)
        }

        // NAVIGATE BACK TO SIGN IN
        // If user already has an account tap here to go back
        binding.tvSignIn.setOnClickListener {
            finish()
            // finish() closes this Activity and goes back to SignIn
            // Same as navigate(-1) or history.back() in React Router
        }
    }

    private fun signUp(
        username: String,
        email: String,
        phone: String,
        github: String,
        password: String
    ) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://godchild.alwaysdata.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        service.signup(username, email, phone, github, password)
            .enqueue(object : Callback<ResponseBody> {

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    showLoading(false)

                    if (response.isSuccessful) {
                        val rawJson = response.body()?.string()
                        val json = JSONObject(rawJson ?: "{}")
                        val message = json.optString("message")

                        if (message.contains("success", ignoreCase = true)) {
                            // Registration successful!
                            // Save user to SharedPreferences so they
                            // don't have to log in again immediately
                            // Same as saving the token after signup in React
                            val prefs = getSharedPreferences(
                                "user_session", Context.MODE_PRIVATE
                            )
                            prefs.edit()
                                .putString("username", username)
                                .putString("email", email)
                                .putString("phone", phone)
                                .putString("github", github)
                                .putString("user_id", "")
                                .apply()

                            // Go straight to MainActivity
                            // No need to go back to Sign In after registering
                            val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                            // this@SignUpActivity specifies which "this" we mean
                            // because inside the Callback "this" refers to
                            // the Callback object not the Activity
                            // Same issue as using arrow functions in React
                            // to preserve "this" context
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            showError(message.ifEmpty { "Registration failed" })
                            // ifEmpty provides a fallback if message is blank
                            // Same as: message || "Registration failed" in JavaScript
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
        binding.btnSignUp.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }
}