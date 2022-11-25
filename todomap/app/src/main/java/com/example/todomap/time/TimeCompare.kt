package com.example.todomap.time

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

object TimeCompare {
    @SuppressLint("SimpleDateFormat")
    fun intervalBetweenDate(beforeDate: String) : Long {
        return (System.currentTimeMillis() - SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(beforeDate)!!.time) // ms초 차이
    }
}