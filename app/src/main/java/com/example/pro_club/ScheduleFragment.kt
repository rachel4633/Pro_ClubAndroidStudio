package com.example.pro_club


import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pro_club.databinding.FragmentScheduleBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScheduleFragment : Fragment() {

    //Fragment is like a mini Activity - it has its own layout
    //and its own lifecycle but lives INSIDE an Activity
    //Think of it like a react component that has its own state
    //and renders inside a parent component(Main activity)
    //Schedule Fragment is the Schedule tab content

    //view binding for fragment_schedule.xml
    //we use a nullable type here (_binding) because in fragments
    //the view can be destroyed while the fragment still exists
    //this is a fragment specific pattern activities don't need

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    //binding (without underscore) is what we use everywhere
    //The !! means "I am sure this is not null right now"
    //get() = _binding !! means every time we access binding
    //it checks _binding and return it safely

    private lateinit var adapter: BlockAdapter
    //lateinit means we will set this up later in on view created
    //same pattern as we used in main activity before

    private val handler = Handler(Looper.getMainLooper())
    //Handler for the live clock - same as setInterval in JavaScript
    //Looper get main looper () means run updates on the UI thread

    private val clockRunnable = object : Runnable {
        override fun run() {
            //this runs every 60 sec to update the clock
            //and check which block is happening NOW
            if (_binding != null) {
                //We check _binding is not null before updating
                //because the fragment might have been destroyed
                //this is the fragment lifecycle safety check
                updateClock()
                adapter.notifyDataSetChanged()
            }
            handler.postDelayed(this, 60000)
            //schedule itself to run again in 60 seconds
            //same as your setInterval(() => {} , 60000 in react
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //onCreateView is called when the Fragment needs to
        //draw its layout for the first time
        //Same as the render() or return () in a react component
        //inflater reads fragment_schedule.xml and trust it into views
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
        //binding.root is the outermost view in fragment_schedule.xml
        //we return it so android  what to display
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //onViewCreated is called RIGHT AFTER onCreateView
        //This is where we set up all our logic
        //same as componentDidMount or useEffect(() => {}, []) in react
        //The view is ready so we can safely access all the views

        setupAdapter()
        updateClock()
        updateProgressBar()
        setupNotificationButton()
        handler.post(clockRunnable)
        //start the clock ticking immediately
    }
    private fun setupAdapter() {
        adapter = BlockAdapter(
            context = requireContext(),
            // requireContext() is Fragment's way of getting Context
            // In Activity we used "this" — in Fragment we use requireContext()
            // It throws an error if the Fragment is not attached
            // which protects us from crashes
            sections = ScheduleData.sections,
            onProgressUpdate = { updateProgressBar() }
        )
        binding.recyclerView.adapter = adapter
      }
    private fun updateClock() {
        // Same logic as before in MainActivity
        // Gets current time and date and updates the TextView
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        val now = Date()
        binding.tvDateTime.text = dateFormat.format(now) + " · " + timeFormat.format(now)
    }

    private fun updateProgressBar() {
        val (done, total) = adapter.getProgress()
        binding.tvProgressLabel.text = done.toString() + " of " + total.toString() + " blocks done"
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
        // onDestroyView is called when the Fragment's view is destroyed
        // We MUST stop the handler here to prevent memory leaks
        // Same as returning a cleanup from useEffect in React:
        // useEffect(() => { return () => clearInterval(timer) }, [])
        handler.removeCallbacks(clockRunnable)
        _binding = null
        // Set _binding to null to free memory
        // This is the Fragment lifecycle cleanup pattern
    }
}



