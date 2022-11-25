package com.example.todomap.todo

import com.google.android.gms.maps.model.LatLng

data class SearchResultEntity(
    val fullAddress: String,
    val name: String,
    val locationLatLng: LatLng //위도 경도 float 형식으로 갖고 있음
)