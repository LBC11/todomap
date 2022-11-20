package com.example.todomap.retrofit.dto

data class TodoEntity(
    val id: Long,
    val uid: String,
    val date: String,
    val location: String,
    val description: String
)
