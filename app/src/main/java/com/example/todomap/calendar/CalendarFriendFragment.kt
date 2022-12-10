package com.example.todomap.calendar

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.todomap.R
import com.example.todomap.databinding.FragmentCalendarBinding
import com.example.todomap.databinding.FragmentCalendarFriendBinding
import com.example.todomap.profile.ProfileFragment
import com.example.todomap.user.UserAccount
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class CalendarFriendFragment(val uid: String) : Fragment() {

    private val TAG = "CalendarFriendFragment"

    private lateinit var context: FragmentActivity
    private lateinit var binding : FragmentCalendarFriendBinding
    private val todoViewModel: TodoViewModel by viewModels()
    private lateinit var adapter: FriendTodoListAdapter

    private lateinit var friendRef: DatabaseReference
    private var firebaseStorage = FirebaseStorage.getInstance().reference

    @Deprecated("Deprecated in Java")
    override fun onAttach(activity: Activity) { // Fragment 가 Activity 에 attach 될 때 호출된다.
        context = activity as FragmentActivity
        super.onAttach(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCalendarFriendBinding.inflate(inflater, container, false)
        friendRef = FirebaseDatabase.getInstance().reference.child("userAccount").child(uid)

        val recyclerView = binding.selectedTodoRecyclerView
        if (recyclerView != null) {
            setRecyclerView(recyclerView)
        }

        // 프로필 사진 받아오기
        firebaseStorage.child(uid + "_profileImg").downloadUrl
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Glide.with(context).load(it.result).into(binding.selectedfriendImg!!)
                } else {
                    Toast.makeText(context, it.exception!!.message, Toast.LENGTH_SHORT).show()
                }
            }

        friendRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "success to load userAccount in DB")
                    val hash = snapshot.value as HashMap<*, *>?

                    Log.d(TAG, "${hash?.get("userName")}, ${hash?.get("info")}")
                    // username, info 설정
                    binding.selectedUserName!!.text = hash?.get("userName").toString()
                    binding.selectedUserInfo!!.text = hash?.get("info").toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Failed to load userAccount in DB")
            }
        })

        todoViewModel.date.observe(viewLifecycleOwner) {
            Log.d(TAG, it.toString())
            lifecycleScope.launch {
                todoViewModel.getAllByDateLive(uid, it.toString()).observe(viewLifecycleOwner) { todoList ->
                    if (todoList != null) {
                        // Adapter 데이터 갱신
                        adapter.setTodoList(todoList)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }

        binding.selectedCalendarView!!.setOnDateChangeListener{ _, year, month,dayOfMonth ->
            val dateStr = "$year-${month+1}-$dayOfMonth"
            todoViewModel.updateDate(dateStr)
        }



        return binding.root
    }

    private fun setRecyclerView(recyclerView: RecyclerView){
        val dateOfToday = getTodayOfDate()
        adapter = FriendTodoListAdapter(todoViewModel)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        todoViewModel.updateDate(dateOfToday)
    }

    private fun getTodayOfDate(): String {
        //오늘 날짜
        val dateOfTodayLong = binding.selectedCalendarView!!.date
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return sdf.format(dateOfTodayLong)
    }
}