package com.example.pro_club

import android.content.Context
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiHelper(private val context: Context) {
    // ApiHelper is a helper class that holds our Retrofit setup
    // We keep it simple now — just the base URL and retrofit instance
    // All actual API calls are made directly in each Activity/Fragment
    // Same as having an axios instance in React:
    // const api = axios.create({ baseURL: "https://..." })

    val BASE_URL = "https://godchild.alwaysdata.net/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: ApiService = retrofit.create(ApiService::class.java)
    // service is ready to use anywhere we need it
    // Just create ApiHelper(context) and call service.anyEndpoint()

    interface CallBack {
        fun onSuccess(result: JSONObject)
        fun onFailure(error: String?)
    }
}