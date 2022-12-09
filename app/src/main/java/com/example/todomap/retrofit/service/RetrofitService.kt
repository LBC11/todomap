package com.example.todomap.retrofit.service

import com.example.todomap.retrofit.api.ApiInterface
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitService {

    companion object {
        private const val baseUrl = "http://127.0.0.1:8081/"

        private var gson = GsonBuilder()
            .setLenient()
            .create()

        private var okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()

        private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val todoService: ApiInterface = retrofit.create(ApiInterface::class.java)
    }
}