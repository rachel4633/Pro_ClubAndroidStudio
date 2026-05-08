package com.example.pro_club

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Phone restarted — reschedule all notifications
            // Same as re-registering timers after a server restart

            val prefs = context.getSharedPreferences(
                "user_session", Context.MODE_PRIVATE
            )
            val userId = prefs.getString("user_id", "0") ?: "0"
            if (userId == "0") return
            // No user logged in — nothing to schedule

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
                    if (response.isSuccessful) {
                        val rawJson = response.body()?.string()
                        val json = JSONObject(rawJson ?: "{}")
                        val blocksArray = json.optJSONArray("blocks") ?: JSONArray()

                        val blocks = mutableListOf<Block>()
                        for (i in 0 until blocksArray.length()) {
                            val obj = blocksArray.getJSONObject(i)
                            blocks.add(Block(
                                id = obj.optInt("id", 0),
                                time = "",
                                title = obj.optString("title", ""),
                                desc = obj.optString("description", ""),
                                type = obj.optString("task_type", "routine"),
                                startHour = obj.optInt("start_hour", 0),
                                startMinute = obj.optInt("start_minute", 0),
                                endHour = obj.optInt("end_hour", 0),
                                endMinute = obj.optInt("end_minute", 0),
                                motivation = obj.optString("motivation", ""),
                                section = getSectionFromTime(obj.optInt("start_hour", 0))
                            ))
                        }
                        // Reschedule all notifications
                        NotificationScheduler.scheduleNotificationsForBlocks(
                            context, blocks
                        )
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    android.util.Log.e("BOOT", "Failed to reschedule: ${t.message}")

                    val retrofit = Retrofit.Builder()
                        .baseUrl("https://godchild.alwaysdata.net/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(okhttp3.OkHttpClient.Builder()
                            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                            .build())
                        .build()
                }
            })

        }
    }

    private fun getSectionFromTime(startHour: Int): String {
        return when {
            startHour in 5..10 -> "Morning"
            startHour in 11..13 -> "Classes"
            startHour in 14..16 -> "Afternoon"
            startHour in 17..21 -> "Evening"
            else -> "General"
        }
    }

}