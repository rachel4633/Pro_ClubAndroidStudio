package com.example.pro_club


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pro_club.databinding.FragmentWeeklyReviewBinding


class WeeklyReviewFragment: Fragment() {

    private var _binding: FragmentWeeklyReviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeeklyReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadWeeklyProgress()
    }


    private fun loadWeeklyProgress() {
        // Get done blocks from SharedPreferences
        // Same storage BlockAdapter uses when marking done
        val prefs = requireContext()
            .getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE)

        // Get total blocks from API via user session
        val userPrefs = requireContext()
            .getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = userPrefs.getString("user_id", "0") ?: "0"

        // Fetch blocks from API to get real total count
        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl("https://godchild.alwaysdata.net/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        service.getBlocks(userId).enqueue(object : retrofit2.Callback<okhttp3.ResponseBody> {
            override fun onResponse(
                call: retrofit2.Call<okhttp3.ResponseBody>,
                response: retrofit2.Response<okhttp3.ResponseBody>
            ) {
                if (response.isSuccessful) {
                    val rawJson = response.body()?.string()
                    val json = org.json.JSONObject(rawJson ?: "{}")
                    val blocksArray = json.optJSONArray("blocks")
                        ?: org.json.JSONArray()

                    // Total blocks from database
                    val total = blocksArray.length()
                    // Same as blocks.length in JavaScript

                    // Count done blocks from SharedPreferences
                    var done = 0
                    for (i in 0 until blocksArray.length()) {
                        val block = blocksArray.getJSONObject(i)
                        val title = block.optString("title", "")
                        if (prefs.getBoolean(title, false)) {
                            done++
                        }
                    }

                    // Get current day of week
                    // Calendar.DAY_OF_WEEK: 1=Sunday, 2=Monday...7=Saturday
                    val calendar = java.util.Calendar.getInstance()
                    val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)

                    val scoreText = "$done/$total blocks ⭐"
                    val emptyText = "0/$total blocks"
                    // Using Kotlin string templates ($variable)
                    // Same as template literals in JavaScript (`${variable}`)

                    // Update each day — highlight today with score
                    binding.tvMondayScore.text = if (dayOfWeek == 2) scoreText else emptyText
                    binding.tvTuesdayScore.text = if (dayOfWeek == 3) scoreText else emptyText
                    binding.tvWednesdayScore.text = if (dayOfWeek == 4) scoreText else emptyText
                    binding.tvThursdayScore.text = if (dayOfWeek == 5) scoreText else emptyText
                    binding.tvFridayScore.text = if (dayOfWeek == 6) scoreText else emptyText
                    binding.tvSaturdayScore.text = if (dayOfWeek == 7) scoreText else emptyText
                    binding.tvSundayScore.text = if (dayOfWeek == 1) scoreText else emptyText
                }
            }

            override fun onFailure(
                call: retrofit2.Call<okhttp3.ResponseBody>,
                t: Throwable
            ) {
                // API failed — show dashes
                val dash = "---"
                binding.tvMondayScore.text = dash
                binding.tvTuesdayScore.text = dash
                binding.tvWednesdayScore.text = dash
                binding.tvThursdayScore.text = dash
                binding.tvFridayScore.text = dash
                binding.tvSaturdayScore.text = dash
                binding.tvSundayScore.text = dash
            }
        })
    }
    //day of week == 2 means monday android counts from sunday
    //if condition value if true value if false
    //same condition ? value if true value if false in java script

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Always clean up binding in onDestroyView
        // Prevents memory leaks in Fragments
    }
    override fun onResume() {
        super.onResume()
        loadWeeklyProgress()
    }
}
