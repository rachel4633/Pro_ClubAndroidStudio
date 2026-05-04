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
        //shared preferences stores done blocks using the block title as key
        //We read from the same storage that block adapter writes
        //so this stays in sync with the schedule tab automatically

        val prefs = requireContext()
            .getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE)


        // Count total blocks in the schedule
        // We need this to show "X/15 blocks" for each day
        val total = ScheduleData.sections.sumOf { it.blocks.size }
        //sum  of adds up the block count from each section
        //same as sections reduce((sum,s) => sum + s.block.length

        //count how many blocks are marked done today
        var done = 0
        for (section in ScheduleData.sections) {
            for (block in section.blocks) {
                if (prefs.getBoolean(block.title, false)) {
                    done++
                }
            }
        }
        // Get the current day of the week
        // Calendar.DAY_OF_WEEK returns 1=Sunday, 2=Monday... 7=Saturday
        val calendar = java.util.Calendar.getInstance()
        val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        // Same as: new Date().getDay() in JavaScript


        //update todays score and show a star next to today's day
        //all other day show 0/total since we only track current day
        //in al full app you would save scores per day

        val scoreText = done.toString() + "/" + total.toString() + " blocks ⭐"
        val emptyText = "0/" + total.toString() + " blocks"

        //update each days score textview
        //we highlight today with a star and show done/total
        //other days show 0/total since we reset daily

        binding.tvMondayScore.text = if (dayOfWeek == 2) scoreText else emptyText
        binding.tvTuesdayScore.text = if (dayOfWeek == 3) scoreText else emptyText
        binding.tvWednesdayScore.text = if (dayOfWeek == 4) scoreText else emptyText
        binding.tvThursdayScore.text = if (dayOfWeek == 5) scoreText else emptyText
        binding.tvFridayScore.text = if (dayOfWeek == 6) scoreText else emptyText
        binding.tvSaturdayScore.text = if (dayOfWeek == 7) scoreText else emptyText
        binding.tvSundayScore.text = if (dayOfWeek == 1) scoreText else emptyText
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
}
