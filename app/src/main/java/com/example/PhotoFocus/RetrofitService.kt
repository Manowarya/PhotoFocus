package com.example.PhotoFocus

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


class RetrofitService {
    val retrofit: ApiService by lazy {
        val httpClient = OkHttpClient.Builder()
        val builder = Retrofit.Builder()
            .baseUrl("https://photofocus-production.up.railway.app")
            .addConverterFactory(GsonConverterFactory.create())

        val retrofit = builder
            .client(httpClient.build())
            .build()
        retrofit.create(ApiService::class.java)
    }
}