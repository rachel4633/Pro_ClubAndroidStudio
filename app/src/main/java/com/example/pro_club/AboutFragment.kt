package com.example.pro_club

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pro_club.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!
    // Same nullable binding pattern as all other fragments
    // Even though About has no logic we still follow the same
    // pattern for consistency and safety

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate fragment_about.xml and return the root view
        // This is the ONLY thing About needs to do —
        // just show the static XML layout, no data to load
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Still clean up even though we have no logic
        // Always follow the Fragment cleanup pattern
    }
}