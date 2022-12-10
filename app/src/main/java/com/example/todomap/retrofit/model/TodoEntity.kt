package com.example.todomap.retrofit.model

data class TodoEntity(
    val id: Long,
    val uid: String,
    val date: String, // 날짜
    val time: String, // 시간
    val locName: String,
    val locLatitude: Double, // 위도
    val locLongitude: Double, // 경도
    val description: String
)
