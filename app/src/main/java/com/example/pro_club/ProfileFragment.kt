package com.example.pro_club

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.pro_club.databinding.FragmentProfileBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var photoUri: Uri? = null
    // photoUri holds the URI of the photo taken or selected
    // URI = Uniform Resource Identifier — like a URL but for local files
    // Same as a file object in JavaScript

    // ─── CAMERA LAUNCHER ────────────────────────────────────
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        // This runs after the camera takes a photo
        // success = true means photo was taken successfully
        if (success && photoUri != null) {
            uploadProfilePicture(photoUri!!)
        }
    }
    // registerForActivityResult is the modern way to handle
    // results from other activities like camera or gallery
    // Same as handling a Promise resolve in JavaScript

    // ─── GALLERY LAUNCHER ───────────────────────────────────
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        // This runs after user picks a photo from gallery
        // uri is the location of the selected image
        if (uri != null) {
            uploadProfilePicture(uri)
        }
    }

    // ─── 1. CREATE THE LAYOUT ───────────────────────────────
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    // ─── 2. SET UP LOGIC ────────────────────────────────────
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserProfile()
        setupCameraButton()
        setupEditProfileButton()
        setupLogoutButton()
    }

    // ─── 3. REFRESH ON RESUME ───────────────────────────────
    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }

    // ─── CAMERA BUTTON SETUP ────────────────────────────────
    private fun setupCameraButton() {
        binding.ivProfilePic.setOnClickListener {
            // Show dialog to choose camera or gallery
            // Same as showing a modal in React
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Update Profile Picture")
                .setItems(arrayOf("📷 Take Photo", "🖼️ Choose from Gallery")) { _, which ->
                    when (which) {
                        0 -> openCamera()
                        // Index 0 = Take Photo
                        1 -> openGallery()
                        // Index 1 = Choose from Gallery
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun openCamera() {
        // Create a file to save the photo
        val photoFile = createImageFile()
        // Get a secure URI for the file using FileProvider
        // FileProvider is needed because Android doesn't allow
        // direct file paths to be shared between apps for security
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "com.example.pro_club.fileprovider",
            photoFile
        )
        // Launch the camera with the URI to save to
        cameraLauncher.launch(photoUri)
    }

    private fun openGallery() {
        // Launch gallery to pick an image
        // "image/*" means accept any image type
        galleryLauncher.launch("image/*")
    }

    private fun createImageFile(): File {
        // Create a unique filename using timestamp
        // Same as Date.now() in JavaScript for unique names
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        val fileName = "JPEG_${timestamp}_"

        // Get the Pictures directory on external storage
        val storageDir = requireContext()
            .getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        // Create a temporary file
        return File.createTempFile(fileName, ".jpg", storageDir)
    }

    private fun uploadProfilePicture(uri: Uri) {
        // Show loading state
        binding.ivProfilePic.alpha = 0.5f
        // alpha = 0.5 makes the image semi-transparent
        // Same as opacity: 0.5 in CSS — shows something is loading

        try {
            // Convert URI to actual file bytes for upload
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return
            inputStream.close()
            // readBytes() reads the entire file into memory as a ByteArray
            // Same as FileReader.readAsArrayBuffer() in JavaScript

            // Get user email for identifying which user to update
            val prefs = requireContext()
                .getSharedPreferences("user_session", Context.MODE_PRIVATE)
            val email = prefs.getString("email", "") ?: ""

            // Build multipart request — same as FormData in JavaScript
            // Multipart is used when uploading files with other text data
            val requestFile = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val photoPart = MultipartBody.Part.createFormData(
                "profile_pic",
                "profile_${System.currentTimeMillis()}.jpg",
                requestFile
            )
            val emailPart = email.toRequestBody("text/plain".toMediaTypeOrNull())

            // Build Retrofit for the upload
            val retrofit = Retrofit.Builder()
                .baseUrl("https://godchild.alwaysdata.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(ApiService::class.java)

            service.uploadProfilePic(emailPart, photoPart)
                .enqueue(object : retrofit2.Callback<okhttp3.ResponseBody> {

                    override fun onResponse(
                        call: retrofit2.Call<okhttp3.ResponseBody>,
                        response: retrofit2.Response<okhttp3.ResponseBody>
                    ) {
                        binding.ivProfilePic.alpha = 1.0f
                        // Restore full opacity

                        if (response.isSuccessful) {
                            val rawJson = response.body()?.string()
                            val json = org.json.JSONObject(rawJson ?: "{}")
                            val filename = json.optString("filename", "")

                            if (filename.isNotEmpty()) {
                                // Save new profile pic filename to SharedPreferences
                                prefs.edit().putString("profile_pic", filename).apply()
                                // Load the new image into the ImageView
                                loadProfileImage(filename)
                                android.widget.Toast.makeText(
                                    requireContext(),
                                    "Profile picture updated!",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            android.widget.Toast.makeText(
                                requireContext(),
                                "Failed to upload picture",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(
                        call: retrofit2.Call<okhttp3.ResponseBody>,
                        t: Throwable
                    ) {
                        binding.ivProfilePic.alpha = 1.0f
                        android.widget.Toast.makeText(
                            requireContext(),
                            "Network error: " + t.message,
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        } catch (e: Exception) {
            binding.ivProfilePic.alpha = 1.0f
            android.widget.Toast.makeText(
                requireContext(),
                "Error reading image",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadProfileImage(filename: String) {
        if (filename.isEmpty() || filename == "default_avatar.png") return
        val imageUrl = "https://godchild.alwaysdata.net/static/profile_pics/$filename"
        com.bumptech.glide.Glide.with(requireContext())
            .load(imageUrl)
            .circleCrop()
            .placeholder(android.R.drawable.ic_menu_myplaces)
            .into(binding.ivProfilePic)
    }

    // ─── LOAD USER DATA FROM SHARED PREFERENCES ─────────────
    private fun loadUserProfile() {
        if (_binding == null) return

        val prefs = requireContext()
            .getSharedPreferences("user_session", Context.MODE_PRIVATE)

        val username = prefs.getString("username", "User")
        val email = prefs.getString("email", "No email found")
        val phone = prefs.getString("phone", "---")
        val github = prefs.getString("github", "---")
        val profilePic = prefs.getString("profile_pic", "")

        binding.tvUsername.text = username
        binding.tvEmail.text = email
        binding.tvPhone.text = phone
        binding.tvGithub.text = github

        loadGithubChart(github ?: "")
        loadProfileImage(profilePic ?: "")
    }

    // ─── LOAD GITHUB CHART IN WEBVIEW ───────────────────────
    private fun loadGithubChart(githubUsername: String) {
        if (_binding == null) return
        if (githubUsername.isEmpty() || githubUsername == "---") return

        val webView = binding.webViewGithub
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true

        val html = """
            <html>
            <body style="margin:0;padding:0;background:#111827;">
            <img src="https://ghchart.rshah.org/$githubUsername" 
                 width="100%" 
                 style="display:block;"/>
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    // ─── EDIT PROFILE BUTTON ────────────────────────────────
    private fun setupEditProfileButton() {
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }
    }

    // ─── LOGOUT BUTTON ──────────────────────────────────────
    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            val prefs = requireContext()
                .getSharedPreferences("user_session", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()

            val schedulePrefs = requireContext()
                .getSharedPreferences("schedule_prefs", Context.MODE_PRIVATE)
            schedulePrefs.edit().clear().apply()

            val intent = Intent(requireContext(), SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    // ─── CLEANUP ────────────────────────────────────────────
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}