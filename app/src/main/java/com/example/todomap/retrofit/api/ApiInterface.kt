package com.example.todomap.retrofit.api

import com.example.todomap.retrofit.dto.TodoCreate
import com.example.todomap.retrofit.dto.TodoEntity
import com.example.todomap.retrofit.dto.TodoUpdate
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {

    @GET("{uid}/")
    fun getTodos(@Path("uid") uid: String): Call<List<TodoEntity>>

    @POST("create")
    fun createTodo(@Body todoCreate: TodoCreate): Call<Long>

    @PATCH("{id}")
    fun updateTodo(@Path("id") id: Long, @Body todoUpdate: TodoUpdate): Call<Long>

    @DELETE("{id}")
    fun deleteTodo(@Path("id") id: Long): Call<Long>
}