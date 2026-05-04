package com.example.pro_club

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("login.php")
    fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<ResponseBody>

    @GET("get_products.php")
    fun getProducts(): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @PUT("update_profile.php")
    fun updateProfile(@Body jsonData: String): Call<ResponseBody>

    @Headers("Content-Type: application/json")
    @HTTP(method = "DELETE", path = "delete_item.php", hasBody = true)
    fun deleteItem(@Body jsonData: String): Call<ResponseBody>
}