package com.example.todomap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todomap.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    private val todoViewModel: TodoViewModel by viewModels()
    private lateinit var adapter: TodoListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView = binding.todoRecyclerView
        setRecyclerView(recyclerView)

        //date LiveDate 변경 감지
        todoViewModel.date.observe(this, androidx.lifecycle.Observer {
            Log.d("date", it.toString())
            todoViewModel.getAllByDate(it).observe(this) { todoList ->
                if (todoList != null) {
                    // Adapter 데이터 갱신
                    adapter.setTodoList(todoList)
                    adapter.notifyDataSetChanged()
                }
            }
        })

        binding.calendarView.setOnDateChangeListener{ _, year, month,dayOfMonth ->
            val dateStr = "$year-${month+1}-$dayOfMonth"
            todoViewModel.updateDate(dateStr)
        }

        binding.todoAddBtn.setOnClickListener {
            val description = binding.todoEditText.text.toString()
            GlobalScope.launch {
                val date = todoViewModel.date.value!!
                todoViewModel.insert(TodoEntity(null, description, date))
            }
        }


    }

    private fun setRecyclerView(recyclerView: RecyclerView){
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
//        recyclerView.adapter = TodoListAdapter()
        val dateOfToday = getTodayOfDate()
        todoViewModel.updateDate(dateOfToday)
    }

    private fun getTodayOfDate(): String {
        //오늘 날짜
        val dateOfTodayLong = binding.calendarView.date
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        return sdf.format(dateOfTodayLong)
    }
}