package com.example.todomap.retrofit.model

data class TodoUpdate(
    val uid: String,
    val date: String,
    val time: String,
    val locLatitude: Double,
    val locLongitude: Double,
    val description: String
)
