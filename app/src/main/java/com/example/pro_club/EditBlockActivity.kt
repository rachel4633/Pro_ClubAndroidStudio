package com.example.pro_club

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
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

class EditBlockActivity : AppCompatActivity() {
    // EditBlockActivity reuses activity_add_block.xml layout
    // We just pre-fill all the fields with existing block data
    // Same as having an edit form in React that starts filled in

    private lateinit var binding: ActivityAddBlockBinding
    private var blockId: String = ""
    // blockId tells us WHICH block to update in the database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the block data passed from BlockAdapter
        // Intent extras are how Android passes data between screens
        // Same as passing props or state via React Router navigate()
        blockId = intent.getStringExtra("block_id") ?: ""
        val title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val taskType = intent.getStringExtra("task_type") ?: "coding"
        val startHour = intent.getStringExtra("start_hour") ?: "0"
        val startMinute = intent.getStringExtra("start_minute") ?: "0"
        val endHour = intent.getStringExtra("end_hour") ?: "0"
        val endMinute = intent.getStringExtra("end_minute") ?: "0"
        val motivation = intent.getStringExtra("motivation") ?: ""
        val section = intent.getStringExtra("section") ?: "Morning"

        // Change the title to show we are editing not adding
        binding.btnAddBlock.text = "Save Changes"

        // Set up spinners first
        setupSpinners(section, taskType)

        // Pre-fill all the fields with existing block data
        // Same as setting defaultValue in a React controlled input
        binding.etTitle.setText(title)
        binding.etDescription.setText(description)
        binding.etStartHour.setText(startHour)
        binding.etStartMinute.setText(startMinute)
        binding.etEndHour.setText(endHour)
        binding.etEndMinute.setText(endMinute)
        binding.etMotivation.setText(motivation)

        // CANCEL BUTTON
        binding.btnCancel.setOnClickListener { finish() }

        // SAVE BUTTON
        binding.btnAddBlock.setOnClickListener {
            val newTitle = binding.etTitle.text.toString().trim()
            val newDesc = binding.etDescription.text.toString().trim()
            val newMotivation = binding.etMotivation.text.toString().trim()
            val newStartHour = binding.etStartHour.text.toString().trim()
            val newStartMinute = binding.etStartMinute.text.toString().trim()
            val newEndHour = binding.etEndHour.text.toString().trim()
            val newEndMinute = binding.etEndMinute.text.toString().trim()
            val newSection = binding.spinnerSection.selectedItem.toString()
            val newTaskType = binding.spinnerType.selectedItem.toString()

            if (newTitle.isEmpty() || newDesc.isEmpty() ||
                newStartHour.isEmpty() || newEndHour.isEmpty()) {
                showError("Please fill in all required fields")
                return@setOnClickListener
            }

            showLoading(true)
            editBlock(newTitle, newDesc, newTaskType, newStartHour,
                newStartMinute, newEndHour, newEndMinute, newMotivation, newSection)
        }
    }

    private fun setupSpinners(currentSection: String, currentType: String) {
        val sections = listOf("Morning", "Classes", "Afternoon", "Evening", "General")
        val sectionAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, sections)
        binding.spinnerSection.adapter = sectionAdapter
        // Pre-select the current section
        binding.spinnerSection.setSelection(
            sections.indexOf(currentSection).takeIf { it >= 0 } ?: 0
        )
        // takeIf { it >= 0 } means use the index only if found
        // otherwise default to 0 (first item)

        val types = listOf("routine", "workout", "coding", "class", "break", "football", "sleep")
        val typeAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, types)
        binding.spinnerType.adapter = typeAdapter
        // Pre-select the current type
        binding.spinnerType.setSelection(
            types.indexOf(currentType).takeIf { it >= 0 } ?: 0
        )
    }

    private fun editBlock(
        title: String, description: String, taskType: String,
        startHour: String, startMinute: String,
        endHour: String, endMinute: String,
        motivation: String, section: String
    ) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://godchild.alwaysdata.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        service.editBlock(
            blockId, title, description, taskType,
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
                    if (json.optString("status") == "success") {
                        // Block updated — close and return to schedule
                        // onResume() in ScheduleFragment will reload the list
                        finish()
                    } else {
                        showError("Failed to update block")
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