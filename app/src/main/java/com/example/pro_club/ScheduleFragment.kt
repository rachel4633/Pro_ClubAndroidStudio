package com.example.pro_club

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pro_club.databinding.FragmentScheduleBinding
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: BlockAdapter
    private val handler = Handler(Looper.getMainLooper())

    private val clockRunnable = object : Runnable {
        override fun run() {
            if (_binding != null) {
                updateClock()
                adapter.notifyDataSetChanged()
            }
            handler.postDelayed(this, 60000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup adapter with empty list first
        // Will be filled by API call below
        adapter = BlockAdapter(
            context = requireContext(),
            sections = emptyList(),
            onProgressUpdate = { updateProgressBar() }
        )
        binding.recyclerView.adapter = adapter

        updateClock()
        setupNotificationButton()
        handler.post(clockRunnable)

        // FAB button — opens AddBlockActivity
        // fabAddTask matches your XML id
        binding.fabAddTask.setOnClickListener {
            val intent = Intent(requireContext(), AddBlockActivity::class.java)
            startActivity(intent)
        }

        // Load blocks from API
        loadBlocksFromApi()
    }

    override fun onResume() {
        super.onResume()
        // Called every time fragment comes back into view
        // So when user returns from AddBlockActivity
        // the list automatically refreshes with new blocks
        // Same as a focus listener in React Native
        if (::adapter.isInitialized) {
            loadBlocksFromApi()
        }
    }

    private fun loadBlocksFromApi() {
        val prefs = requireContext()
            .getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://godchild.alwaysdata.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        val userId = prefs.getString("user_id", "0") ?: "0"
        android.util.Log.d("SCHEDULE", "Loading blocks for user_id: $userId")

        service.getBlocks(userId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    val rawJson = response.body()?.string()
                    val json = JSONObject(rawJson ?: "{}")
                    val blocksArray = json.optJSONArray("blocks") ?: JSONArray()

                    val blocks = parseBlocks(blocksArray)

                    if (blocks.isEmpty()) {
                        // No blocks in database yet
                        // Fall back to hardcoded schedule
                        // So new users still see something on screen
                        adapter.updateSections(ScheduleData.sections)
                    } else {
                        val sections = groupIntoSections(blocks)
                        adapter.updateSections(sections)
                    }
                    updateProgressBar()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // API failed — use hardcoded schedule as fallback
                // App still works offline this way
                adapter.updateSections(ScheduleData.sections)
                updateProgressBar()
            }
        })
    }

    private fun parseBlocks(blocksArray: JSONArray): List<Block> {
        val blocks = mutableListOf<Block>()

        for (i in 0 until blocksArray.length()) {
            val obj = blocksArray.getJSONObject(i)

            val startHour = obj.optInt("start_hour", 0)
            val startMinute = obj.optInt("start_minute", 0)
            val endHour = obj.optInt("end_hour", 0)
            val endMinute = obj.optInt("end_minute", 0)

            // Build display time string e.g. "7:30 – 11:00"
            val time = String.format(
                "%d:%02d – %d:%02d",
                startHour, startMinute, endHour, endMinute
            )

            blocks.add(Block(
                id = obj.optInt("id", 0),
                time = time,
                title = obj.optString("title", ""),
                desc = obj.optString("description", ""),
                type = obj.optString("task_type", "routine"),
                notifyAt = "",
                startHour = startHour,
                startMinute = startMinute,
                endHour = endHour,
                endMinute = endMinute,
                motivation = obj.optString("motivation", ""),
                section = obj.optString("section", "General")
            ))
        }
        return blocks
    }

    private fun groupIntoSections(blocks: List<Block>): List<Section> {
        // Group blocks by section name
        // Same as lodash groupBy in JavaScript
        val sectionOrder = listOf(
            "Morning", "Classes", "Afternoon", "Evening", "General"
        )

        val grouped = blocks.groupBy { it.section }

        val sections = mutableListOf<Section>()

        for (sectionName in sectionOrder) {
            val sectionBlocks = grouped[sectionName]
            if (!sectionBlocks.isNullOrEmpty()) {
                val sorted = sectionBlocks.sortedWith(
                    compareBy({ it.startHour }, { it.startMinute })
                )
                val first = sorted.first()
                val last = sorted.last()
                val timeRange = String.format(
                    "%d:%02d – %d:%02d",
                    first.startHour, first.startMinute,
                    last.endHour, last.endMinute
                )
                sections.add(Section(
                    section = sectionName,
                    timeRange = timeRange,
                    blocks = sorted
                ))
            }
        }
        return sections
    }

    private fun updateClock() {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        val now = Date()
        binding.tvDateTime.text = dateFormat.format(now) + " · " + timeFormat.format(now)
    }

    private fun updateProgressBar() {
        val (done, total) = adapter.getProgress()
        binding.tvProgressLabel.text =
            done.toString() + " of " + total.toString() + " blocks done"
        val percent = if (total > 0) (done * 100 / total) else 0
        binding.tvProgressPercent.text = percent.toString() + "%"

        binding.progressBar.post {
            val parent = binding.progressBar.parent as ViewGroup
            val totalWidth = parent.width
            val targetWidth = (totalWidth * percent / 100)
            val params = binding.progressBar.layoutParams
            params.width = targetWidth
            binding.progressBar.layoutParams = params
        }
    }

    private fun setupNotificationButton() {
        binding.btnNotifications.setOnClickListener {
            binding.btnNotifications.text = "Notifications On"
            binding.btnNotifications.isEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(clockRunnable)
        _binding = null
    }
}