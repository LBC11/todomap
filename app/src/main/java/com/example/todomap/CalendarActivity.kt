package com.example.todomap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.todomap.databinding.ActivityMainBinding
import java.util.Calendar

class CalendarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val calendar : Calendar = Calendar.getInstance()

        val date = binding.calendarView.date
        binding.calendarText.text = date.toString()


    }
}