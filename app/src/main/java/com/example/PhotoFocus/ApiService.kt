package com.example.PhotoFocus

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("/get-templates/{id}")
    suspend fun getTemplate(@Path("id") id: String): Response<EditImageActivity.Templates>

    @POST("/delete-template")
    fun deleteTemplate(@Body requestBody: RequestBody): Call<String>

    @POST("/save-template")
    fun saveTemplate(@Body requestBody: RequestBody): Call<String>

    @POST("/verification")
    fun registerUser(@Body requestBody: RequestBody): Call<String>

    @POST("/register")
    fun sendCode(@Body body: RequestBody): Call<String>

    @POST("/authorization")
    fun authorization(@Body body: RequestBody): Call<String>
}