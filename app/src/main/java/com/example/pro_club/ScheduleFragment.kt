package com.example.pro_club

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
    private lateinit var deleteReceiver: BroadcastReceiver
    // This listens for messages from BlockAdapter when a block is deleted
    // Same as window.addEventListener("blockDeleted", () => reload()) in JS

    private val clockRunnable = object : Runnable {
        override fun run() {
            // Check if fragment is still attached AND binding exists
            // before trying to update the UI
            // Same as checking if React component is still mounted
            if (_binding != null && isAdded) {
                updateClock()
                if (::adapter.isInitialized) {
                    adapter.notifyDataSetChanged()
                }
            }
            // Always reschedule even if we skipped the update
            handler.postDelayed(this, 60000)
        }
    }

    // ─── 1. CREATE THE LAYOUT ───────────────────────────────
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    // ─── 2. SET UP LOGIC AFTER LAYOUT IS READY ──────────────
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup adapter with empty list first
        // Will be filled by API call below
        // Same as useState([]) in React — start empty, fill later
        adapter = BlockAdapter(
            context = requireContext(),
            sections = emptyList(),
            onProgressUpdate = { updateProgressBar() },
            onDataChanged = {
                // Called when a block is deleted or changed
                // Reload the list from API immediately
                // Same as calling fetchBlocks() after any mutation in React
                loadBlocksFromApi()
            }
        )
        binding.recyclerView.adapter = adapter

        updateClock()
        setupNotificationButton()
        setupDeleteReceiver()
        handler.post(clockRunnable)

        // FAB button — opens AddBlockActivity
        binding.fabAddTask.setOnClickListener {
            val intent = Intent(requireContext(), AddBlockActivity::class.java)
            startActivity(intent)
        }

        loadBlocksFromApi()
    }

    // ─── 3. REFRESH WHEN COMING BACK TO THIS SCREEN ─────────
    override fun onResume() {
        super.onResume()
        // Called every time fragment comes back into view
        // Small delay so API has time to save before we fetch
        // Same as setTimeout(() => fetchBlocks(), 500) in JavaScript
        if (::adapter.isInitialized) {
            handler.postDelayed({
                if (isAdded && _binding != null) {
                    loadBlocksFromApi()
                }
            }, 500)
            // 500ms = half a second
            // Gives the server time to finish saving
            // before we fetch the updated list
        }
    }

    // ─── LOAD BLOCKS FROM API ───────────────────────────────
    private fun loadBlocksFromApi() {
        // Safety check — don't load if fragment is detached
        // Prevents crash if called after fragment is destroyed
        // Same as checking if React component is still mounted
        if (!isAdded || _binding == null) return

        val prefs = requireContext()
            .getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = prefs.getString("user_id", "0") ?: "0"
        android.util.Log.d("SCHEDULE", "Loading blocks for user_id: $userId")

        val retrofit = Retrofit.Builder()
            .baseUrl("https://godchild.alwaysdata.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        service.getBlocks(userId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                // Safety check inside callback too
                // The fragment might be destroyed by the time
                // the API responds — check before updating UI
                if (!isAdded || _binding == null) return

                if (response.isSuccessful) {
                    val rawJson = response.body()?.string()
                    val json = JSONObject(rawJson ?: "{}")
                    val blocksArray = json.optJSONArray("blocks") ?: JSONArray()
                    val blocks = parseBlocks(blocksArray)

                    if (blocks.isEmpty()) {
                        // No blocks in database yet
                        // Fall back to hardcoded schedule
                        // So new users still see something on screen
                        adapter.updateSections(emptyList())
                    } else {
                        val sections = groupIntoSections(blocks)
                        adapter.updateSections(sections)
                    }
                    updateProgressBar()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if (!isAdded || _binding == null) return
                // API failed — use hardcoded schedule as fallback
                // App still works offline this way
                adapter.updateSections(emptyList())
                updateProgressBar()
            }
        })
    }

    // ─── PARSE JSON BLOCKS INTO BLOCK OBJECTS ───────────────
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

    // ─── GROUP BLOCKS INTO SECTIONS ─────────────────────────
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

    // ─── UPDATE CLOCK DISPLAY ───────────────────────────────
    private fun updateClock() {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        val now = Date()
        binding.tvDateTime.text = dateFormat.format(now) + " · " + timeFormat.format(now)
    }

    // ─── UPDATE PROGRESS BAR ────────────────────────────────
    private fun updateProgressBar() {
        val (done, total) = adapter.getProgress()
        binding.tvProgressLabel.text =
            done.toString() + " of " + total.toString() + " blocks done"
        val percent = if (total > 0) (done * 100 / total) else 0
        binding.tvProgressPercent.text = percent.toString() + "%"

        binding.progressBar.post {
            if (_binding == null) return@post
            val parent = binding.progressBar.parent as ViewGroup
            val totalWidth = parent.width
            val targetWidth = (totalWidth * percent / 100)
            val params = binding.progressBar.layoutParams
            params.width = targetWidth
            binding.progressBar.layoutParams = params
        }
    }

    // ─── NOTIFICATION BUTTON ────────────────────────────────
    private fun setupNotificationButton() {
        // Check saved notification state from SharedPreferences
        val prefs = requireContext()
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val notifEnabled = prefs.getBoolean("notifications_enabled", false)
        updateNotificationButton(notifEnabled)

        binding.btnNotifications.setOnClickListener {
            // Toggle on/off — same as toggleDone() in React
            val currentState = prefs.getBoolean("notifications_enabled", false)
            val newState = !currentState
            prefs.edit().putBoolean("notifications_enabled", newState).apply()
            updateNotificationButton(newState)
        }
    }

    private fun updateNotificationButton(isEnabled: Boolean) {
        if (isEnabled) {
            binding.btnNotifications.text = "🔔 Notifications On"
            binding.btnNotifications.setTextColor(
                android.graphics.Color.parseColor("#22C55E")
            )
            // Green — notifications are on
        } else {
            binding.btnNotifications.text = "🔕 Notifications Off"
            binding.btnNotifications.setTextColor(
                android.graphics.Color.parseColor("#8899BB")
            )
            // Muted — notifications are off
        }
        binding.btnNotifications.isEnabled = true
    }

    // ─── DELETE RECEIVER ────────────────────────────────────
    private fun setupDeleteReceiver() {
        // BroadcastReceiver listens for delete events from BlockAdapter
        // Same as window.addEventListener("blockDeleted", reload) in JS
        deleteReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                loadBlocksFromApi()
            }
        }

        androidx.core.content.ContextCompat.registerReceiver(
            requireContext(),
            deleteReceiver,
            IntentFilter("com.example.pro_club.BLOCK_DELETED"),
            androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    // ─── CLEANUP ────────────────────────────────────────────
    override fun onDestroyView() {
        super.onDestroyView()
        // Stop the clock — prevents memory leak
        // Same as clearInterval() in JavaScript
        handler.removeCallbacks(clockRunnable)

        // Unregister delete receiver safely
        // try/catch prevents crash if already unregistered
        if (::deleteReceiver.isInitialized) {
            try {
                requireContext().unregisterReceiver(deleteReceiver)
            } catch (e: Exception) {
                android.util.Log.d("SCHEDULE", "Receiver already unregistered")
            }
        }
        _binding = null
    }
}