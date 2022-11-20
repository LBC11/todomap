package com.example.todomap.retrofit.dto

data class TodoUpdate(
    val uid: String,
    val date: String,
    val location: String,
    val description: String
)
