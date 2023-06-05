package com.example.PhotoFocus

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("/fetch-values")
    suspend fun getTemplate(): Response<ResponseBody>

    @POST("/save-template")
    suspend fun saveTemplate(@Body requestBody: RequestBody): Response<ResponseBody>

    @POST("/verification")
    fun registerUser(@Body requestBody: RequestBody): Call<String>

    @POST("/register")
    fun sendCode(@Body body: RequestBody): Call<String>

    @POST("/authorization")
    fun authorization(@Body body: RequestBody): Call<Int>
}