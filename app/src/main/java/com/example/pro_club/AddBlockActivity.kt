package com.example.pro_club

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.pro_club.databinding.ActivityAddBlockBinding
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AddBlockActivity : AppCompatActivity() {
    // This is the form screen for adding a new schedule block
    // Same as your Addproducts.jsx in the SokoGarden React app
    // User fills in the form, hits Add Block, data goes to the API


    private lateinit var binding: ActivityAddBlockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupButtons()

    }


    private fun setupSpinners() {

        // Block type dropdown options
        val types = listOf("routine", "workout", "study", "class", "break","sleep","teach","meeting","Go Somewhere","e.t.c")
        val typeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            types
        )
        binding.spinnerType.adapter = typeAdapter
    }

    private fun setupButtons() {
        // CANCEL BUTTON — goes back without saving
        binding.btnCancel.setOnClickListener {
            finish()
            // finish() closes this Activity and returns to ScheduleFragment
            // Same as navigate(-1) in React Router
        }

        // ADD BLOCK BUTTON — validates and submits
        binding.btnAddBlock.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val motivation = binding.etMotivation.text.toString().trim()
            val startHour = binding.etStartHour.text.toString().trim()
            val startMinute = binding.etStartMinute.text.toString().trim()
            val endHour = binding.etEndHour.text.toString().trim()
            val endMinute = binding.etEndMinute.text.toString().trim()


            // Auto assign section based on time
            // User doesn't need to manually pick — less confusion
            val taskType = (binding.etTaskType.text.toString()
                .trim()
                .lowercase()
                .ifEmpty { "routine" })
            // lowercase() so "Coding" and "coding" both work the same
            // Same as .toLowerCase() in JavaScript
            // .selectedItem.toString() gets the currently selected
            // dropdown value — same as e.target.value in React onChange

            // VALIDATE
            if (title.isEmpty() || description.isEmpty() ||
                startHour.isEmpty() || startMinute.isEmpty() ||
                endHour.isEmpty() || endMinute.isEmpty()) {
                showError("Please fill in all required fields")
                return@setOnClickListener
            }

            // Validate time ranges
            val startH = startHour.toIntOrNull()
            val startM = startMinute.toIntOrNull()
            val endH = endHour.toIntOrNull()
            val endM = endMinute.toIntOrNull()
            // toIntOrNull() converts String to Int safely
            // Returns null if the string is not a valid number
            // Same as parseInt() in JavaScript but safer

            if (startH == null || startM == null || endH == null || endM == null) {
                showError("Please enter valid numbers for time")
                return@setOnClickListener
            }

            if (startH < 0 || startH > 23 || endH < 0 || endH > 23) {
                showError("Hours must be between 0 and 23")
                return@setOnClickListener
            }

            if (startM < 0 || startM > 59 || endM < 0 || endM > 59) {
                showError("Minutes must be between 0 and 59")
                return@setOnClickListener
            }
            val section = getSectionFromTime(startH)

            showLoading(true)
            addBlock(title, description, taskType, startHour,
                startMinute, endHour, endMinute, motivation, section)
        }
    }

    private fun getSectionFromTime(startHour: Int): String {
        // Automatically determine section based on start hour
        // So user doesn't have to manually pick section
        // Same as a computed property in React
        return when {
            startHour in 5..11 -> "Morning"
            // 5am to 11am = Morning
            startHour in 11..13 -> "Classes"
            // 11am to 1pm = Classes
            startHour in 13..17 -> "Afternoon"
            // 1pm to 5pm = Afternoon
            startHour in 17..21 -> "Evening"
            // 5pm to 9pm = Evening
            else -> "General"
            // Everything else = General
        }
    }

    private fun addBlock(
        title: String,
        description: String,
        taskType: String,
        startHour: String,
        startMinute: String,
        endHour: String,
        endMinute: String,
        motivation: String,
        section: String
    ) {
        // Get the logged in user's ID from SharedPreferences
        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = prefs.getString("user_id", "1") ?: "1"
        // Same as localStorage.getItem("user_id") in React

        // Temporary debug toast
        android.widget.Toast.makeText(
            this,
            "Adding for user: $userId",
            android.widget.Toast.LENGTH_LONG
        ).show()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://godchild.alwaysdata.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        service.addBlock(
            userId, title, description, taskType,
            startHour, startMinute, endHour, endMinute,
            motivation, section
        ).enqueue(object : Callback<ResponseBody> {

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
                        // Block added successfully!
                        // finish() closes this screen and returns
                        // to ScheduleFragment which will reload
                        // the blocks from the API via onResume()
                        finish()
                    } else {
                        showError(message.ifEmpty { "Failed to add block" })
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
        binding.btnAddBlock.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

}
