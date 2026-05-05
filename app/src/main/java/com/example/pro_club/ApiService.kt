package com.example.pro_club

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("api/signin")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("api/signup")
    fun signup(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("phone") phone: String,
        @Field("github_username") github: String,
        @Field("password") password: String
    ): Call<ResponseBody>

    @GET("api/get_blocks")
    fun getBlocks(
        @Query("user_id") userId: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("api/add_block")
    fun addBlock(
        @Field("user_id") userId: String,
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("task_type") taskType: String,
        @Field("start_hour") startHour: String,
        @Field("start_minute") startMinute: String,
        @Field("end_hour") endHour: String,
        @Field("end_minute") endMinute: String,
        @Field("motivation") motivation: String,
        @Field("section") section: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @PUT("api/edit_block")
    fun editBlock(
        @Field("block_id") blockId: String,
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("task_type") taskType: String,
        @Field("start_hour") startHour: String,
        @Field("start_minute") startMinute: String,
        @Field("end_hour") endHour: String,
        @Field("end_minute") endMinute: String,
        @Field("motivation") motivation: String,
        @Field("section") section: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "api/delete_block", hasBody = true)
    fun deleteBlock(
        @Field("block_id") blockId: String
    ): Call<ResponseBody>
}