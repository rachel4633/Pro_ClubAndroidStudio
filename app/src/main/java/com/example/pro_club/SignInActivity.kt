package com.example.pro_club

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.pro_club.databinding.ActivitySignInBinding
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SignInActivity : AppCompatActivity() {
    // SignInActivity is a completely separate screen from MainActivity
    // It shows BEFORE the main app — like the login page on any app
    // Same as your /signIn route in React that redirects to / after login

    private lateinit var binding: ActivitySignInBinding
    // ViewBinding for activity_sign_in.xml
    // Gives us access to etEmail, etPassword, btnSignIn etc

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // CHECK IF USER IS ALREADY LOGGED IN
        // When the app opens we check SharedPreferences for a saved session
        // If a session exists we skip the login screen entirely
        // Same as checking localStorage for a token in React:
        // if (localStorage.getItem("token")) navigate("/")
        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val savedUsername = prefs.getString("username", null)
        val savedUserId = prefs.getString("user_id", null)

        // Check if session is valid — must have both username AND a real user_id
        if (savedUsername != null &&
            savedUserId != null &&
            savedUserId != "null" &&
            savedUserId.isNotEmpty()) {
            goToMain()
            return
        }
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        )

        // If user_id is invalid clear the session and force re-login
        if (savedUsername != null && (savedUserId == null || savedUserId == "null" || savedUserId == "0")) {
            prefs.edit().clear().apply()
            // Clear bad session so user logs in fresh
        }

        // SIGN IN BUTTON CLICK
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            // .text.toString() gets the text from the EditText as a String
            // .trim() removes any accidental spaces — same as .trim() in JS

            // VALIDATE — make sure fields are not empty
            // Same as your form validation in React before calling the API
            if (email.isEmpty() || password.isEmpty()) {
                showError("Please fill in all fields")
                return@setOnClickListener
                // return@setOnClickListener stops this click handler
                // Same as: return in a React onClick handler
            }

            // Show loading spinner and disable button
            // Prevents double tapping while API call is running
            // Same as setLoading(true) in React
            showLoading(true)

            // CALL THE API
            signIn(email, password)
        }

        // NAVIGATE TO SIGN UP
        // When user taps "Sign Up" text navigate to SignUpActivity
        // Same as <Link to="/signup"> in React Router
        binding.tvSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signIn(email: String, password: String) {
        // Build a Retrofit instance to call the API
        // Same as axios.post() in your React SokoGarden app
        val retrofit = Retrofit.Builder()
            .baseUrl("https://godchild.alwaysdata.net/")
            // Same BASE_URL as your ApiHelper
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        // Creates the API service from your ApiService interface
        // Same as how Retrofit maps your function calls to HTTP requests

        service.login(email, password).enqueue(object : Callback<ResponseBody> {
            // .enqueue() runs the API call in the BACKGROUND
            // So the UI doesn't freeze while waiting for a response
            // Same as await axios.post() running asynchronously in React

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                // This runs when the server responds — success OR failure
                showLoading(false)
                // Hide spinner regardless of result

                if (response.isSuccessful) {
                    // Server responded with 200 OK
                    val rawJson = response.body()?.string()
                    // Get the raw JSON string from the response body
                    // Same as response.data in axios

                    val json = JSONObject(rawJson ?: "{}")
                    // Parse the JSON string into a JSONObject
                    // Same as JSON.parse(response.data) in JavaScript
                    // ?: "{}" means use empty object if rawJson is null

                    val message = json.optString("message")
                    // optString safely gets a string value from JSON
                    // Same as json?.message in optional chaining JavaScript

                    if (message.contains("success", ignoreCase = true)) {
                        // Login successful!
                        // ignoreCase = true means "Success" and "success" both match

                        val user = json.optJSONObject("user")
                        // Get the user object from the response
                        // Same as response.data.user in axios

                        // Save user details to SharedPreferences
                        // Same as localStorage.setItem() in React
                        val prefs = getSharedPreferences(
                            "user_session", Context.MODE_PRIVATE
                        )
                        val userId = user?.optInt("id", 0) ?: 0
                        prefs.edit()
                            .putString("username", user?.optString("username") ?: email)
                            .putString("email", user?.optString("email") ?: email)
                            .putString("phone", user?.optString("phone") ?: "")
                            .putString("github", user?.optString("github_username") ?: "")
                            .putString("user_id", userId.toString())
                            .putString("profile_pic", user?.optString("profile_pic") ?: "")
                            .apply()

                        //log to verify
                        android.util.Log.d("SIGNIN", "Saved user_id: $userId")
                        // Save all user fields — ProfileFragment reads these later

                        goToMain()
                        // Navigate to the main app
                    } else {
                        showError("Invalid email or password")
                    }
                } else {
                    showError("Login failed — please try again")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // This runs if there was a network error
                // Like a catch block in try/catch in JavaScript
                showLoading(false)
                showError("Network error: " + t.message)
            }
        })
    }

    private fun goToMain() {
        // Navigate to MainActivity and clear the back stack
        // FLAG_ACTIVITY_NEW_TASK + FLAG_ACTIVITY_CLEAR_TASK means
        // the user cannot press Back to return to the login screen
        // Same as navigate("/", { replace: true }) in React Router
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        // finish() destroys this Activity — frees memory
    }

    private fun showLoading(isLoading: Boolean) {
        // Show or hide the loading spinner
        // Disable or enable the sign in button while loading
        // Same as setLoading(true/false) in React
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSignIn.isEnabled = !isLoading
        // !isLoading means: if loading=true then isEnabled=false
        // Button is disabled while API call is running
    }

    private fun showError(message: String) {
        // Show the error TextView with a message
        // Same as setError("Invalid credentials") in React
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }
}