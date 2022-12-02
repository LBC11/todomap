package com.example.todomap.retrofit.api

import com.example.todomap.retrofit.model.TodoCreate
import com.example.todomap.retrofit.model.TodoEntity
import com.example.todomap.retrofit.model.TodoUpdate
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {

    @GET("{uid}/")
    suspend fun getTodos(@Path("uid") uid: String): List<TodoEntity>

    @GET("{uid}/{date}")
    suspend fun getTodosByDate(@Path("uid") uid: String, @Path("date") date: String): List<TodoEntity>

    @POST("create")
    suspend fun createTodo(@Body todoCreate: TodoCreate)

    @PATCH("{id}")
    suspend fun updateTodo(@Path("id") id: Long,  @Body todoUpdate: TodoUpdate)

    @DELETE("{id}")
    suspend fun deleteTodo(@Path("id") id: Long)
}