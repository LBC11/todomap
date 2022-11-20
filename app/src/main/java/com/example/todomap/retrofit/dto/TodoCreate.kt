package com.example.todomap.retrofit.dto

data class TodoCreate(
    val uid: String,
    val date: String,
    val location: String,
    val description: String
)
