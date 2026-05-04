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

        //update the textview with the saved user data
        binding.tvUsername.text = username
        binding.tvEmail.text = email
        binding.tvPhone.text = phone
        binding.tvGithub.text = github
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

            //Navigate to SignInActivity and clear the back stack
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



