package com.example.pro_club

import android.content.Context
import android.content.Intent
import android.widget.Toast
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiHelper(private val context: Context) {

    private val BASE_URL = "https://godchild.alwaysdata.net/"
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(ApiService::class.java)

    // POST LOGIN
    fun postLogin(password: String) {
        val username = "godchild" // Your AlwaysData user
        
        service.login(username, password).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val rawJson = response.body()?.string()
                    val jsonObject = JSONObject(rawJson ?: "{}")

                    if (jsonObject.optString("message") == "Login success") {
                        val user = jsonObject.optJSONObject("user")
                        
                        // Save to SharedPreferences
                        val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
                        prefs.edit().apply {
                            putString("username", user?.optString("username") ?: "godchild")
                            putString("email", user?.optString("email") ?: "")
                            apply()
                        }

                        Toast.makeText(context, "Welcome ${user?.optString("username")}", Toast.LENGTH_LONG).show()
                        
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    }
                } else {
                    Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // GENERIC GET
    fun get(callBack: CallBack) {
        service.getProducts().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val rawJson = response.body()?.string()
                callBack.onSuccess(JSONObject(rawJson ?: "{}"))
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callBack.onFailure(t.message)
            }
        })
    }

    // PUT
    fun put(jsonData: JSONObject) {
        service.updateProfile(jsonData.toString()).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Toast.makeText(context, "Update Successful", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(context, "Update Failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    interface CallBack {
        fun onSuccess(result: JSONObject)
        fun onFailure(error: String?)
    }
}