package com.example.todomap.calendar

import android.R
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todomap.databinding.FragmentCalendarBinding
import com.example.todomap.retrofit.model.TodoCreate
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class CalendarFragment : Fragment() {

    companion object {
        private const val TAG: String = "CalendarFragment"
    }

    private lateinit var binding : FragmentCalendarBinding
    private val todoViewModel: TodoViewModel by viewModels()
    private lateinit var adapter: TodoListAdapter

    private lateinit var firebaseAuth: FirebaseAuth

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        val uid = firebaseAuth.currentUser?.uid.toString()

        val recyclerView = binding.todoRecyclerView
        setRecyclerView(recyclerView)

        //date LiveDate 변경 감지
        todoViewModel.date.observe(viewLifecycleOwner) {
            Log.d("date", it.toString())
            lifecycleScope.launch {
                todoViewModel.getAllByDate(uid, it.toString()).observe(viewLifecycleOwner) { todoList ->
                    if (todoList != null) {
                        // Adapter 데이터 갱신
                        adapter.setTodoList(todoList)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }

        binding.calendarView.setOnDateChangeListener{ _, year, month,dayOfMonth ->
            val dateStr = "$year-${month+1}-$dayOfMonth"
            todoViewModel.updateDate(dateStr)
        }

        // 시간 설정
        var time = ""
        binding.setTimeBtn.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = OnTimeSetListener { view, hourOfDay, minute ->
                time = "${hourOfDay}:${minute}"
                Log.d(TAG, "time picker: ${time}")
                // calendar object 를 알람에 사용
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
            }
            TimePickerDialog(context, R.style.Theme_Material_Light_Dialog_NoActionBar,
                timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),true).show()
        }

        //위치 설정
        val locLatitude = 0.0
        val locLongitude = 0.0
        val locationName = ""
        binding.setLocaBtn.setOnClickListener {

        }

        binding.todoAddBtn.setOnClickListener {
            val description = binding.todoEditText.text.toString()
            GlobalScope.launch(Dispatchers.IO) {
                val date = todoViewModel.date.value!!
                todoViewModel.insert(TodoCreate(uid, date, time, locLatitude , locLongitude , description))
            }
        }
        return binding.root
    }

    private fun setRecyclerView(recyclerView: RecyclerView){
        val dateOfToday = getTodayOfDate()
        adapter = TodoListAdapter(todoViewModel)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        todoViewModel.updateDate(dateOfToday)
    }

    private fun getTodayOfDate(): String {
        //오늘 날짜
        val dateOfTodayLong = binding.calendarView.date
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return sdf.format(dateOfTodayLong)
    }
}