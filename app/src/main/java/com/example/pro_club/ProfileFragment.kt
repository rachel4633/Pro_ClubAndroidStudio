package com.example.pro_club

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pro_club.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {


    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserProfile()
        setupLogoutButton()
    }

    private fun loadUserProfile() {
        //Read user details from sharedpreferences
        //these were saved when the user logged in via ApiHelper
        //same as reading from localstorage in react:
        //const user = JSON.parse(localstorage,getItem("user")

        val prefs = requireContext()
            .getSharedPreferences("user_session", Context.MODE_PRIVATE)

        //read each value using the same keys Apihelper saved them

        val username = prefs.getString("username", "User")
        //getSting(key,default value) - return "user" if key not
        val email = prefs.getString("email", "No email found")
        val phone = prefs.getString("phone", "---")
        val github = prefs.getString("github", "---")
        val profilePic = prefs.getString("profile_pic", "")

        //update the textview with the saved user data
        binding.tvUsername.text = username
        binding.tvEmail.text = email
        binding.tvPhone.text = phone
        binding.tvGithub.text = github

        //load github chart
        loadGithubChart(github ?: "")

        // Load profile picture if exists
        if (!profilePic.isNullOrEmpty() && profilePic != "default_avatar.png") {
            val imageUrl = "https://godchild.alwaysdata.net/static/profile_pics/$profilePic"
            // Load image using Glide
            // Glide is an image loading library — same as next/image in React
            // It handles downloading, caching and displaying images
            com.bumptech.glide.Glide.with(requireContext())
                .load(imageUrl)
                .circleCrop()
                // circleCrop() makes the image circular
                // Like border-radius: 50% in CSS
                .placeholder(android.R.drawable.ic_menu_myplaces)
                // placeholder shows while image loads
                // Same as a skeleton loader in React
                .into(binding.ivProfilePic)
        }
    }
    private fun loadGithubChart(githubUsername: String) {
        if (githubUsername.isEmpty() || githubUsername == "---") return
        // Don't load if no GitHub username saved

        val webView = binding.webViewGithub
        webView.settings.javaScriptEnabled = true
        // Enable JavaScript so the chart renders properly
        // Same as allowing scripts in an iframe

        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        // These make the chart fit the WebView width properly

        // Load the GitHub chart image as HTML
        // ghchart.rshah.org generates a contribution chart image
        // Same as your <img src={ghchart.rshah.org/${username}}> in React
        val html = """
        <html>
        <body style="margin:0;padding:0;background:#111827;">
        <img src="https://ghchart.rshah.org/$githubUsername" 
             width="100%" 
             style="display:block;"/>
        </body>
        </html>
    """.trimIndent()
        // trimIndent() removes the leading spaces from each line
        // Same as a template literal in JavaScript

        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        // loadDataWithBaseURL loads HTML content directly into the WebView
        // Same as setting innerHTML in JavaScript
    }
    private fun setupLogoutButton(){
        binding.btnLogout.setOnClickListener {
            //clear the user session from shared preferences
            //same as localstorage remove item (user) in react
            val prefs = requireContext()
                .getSharedPreferences("user_session", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            // .clear() removes all saved data from tis prefs file
            //,apply () saves the changes permanently

            val schedulePrefs = requireContext()
                .getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE)
            schedulePrefs.edit().clear().apply()
            //Navigate to SignInActivity and clear the back stack
            val intent = Intent(requireContext(), SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            //FLAG ACTIVITY NEW TASK + FLAG ACTIVITY TASK means
            //start signin fresh and remove all previous screens
            //so the user cannot press back to get to the main app
            //same as navigate ("/signin",{ replace: true}) in react router

//            val intent = Intent(requireContext(), SignInActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
//                    Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



