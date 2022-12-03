package com.example.todomap.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todomap.databinding.ActivityUsersearchBinding
import com.example.todomap.user.UserAccount

class UsersearchActivity : AppCompatActivity() {

    private val binding by lazy { ActivityUsersearchBinding.inflate(layoutInflater) }
    lateinit var rv_phone_book: RecyclerView
    lateinit var adapter: UsersearchAdapter
    lateinit var users: ArrayList<UserAccount>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        users = callUserAccounts()

        // Realtime DB에서 결과 가져오기gh


        // Adapter
        adapter = UsersearchAdapter(users, this)
        binding.userRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.userRecyclerView.adapter = adapter


        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 텍스트 검색 누를 때
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 텍스트 변할 때 호출
                adapter.filter.filter(newText)
                return false
            }
        })
    }

    fun callUserAccounts(): ArrayList<UserAccount> {
        TODO("implement this!!!!!")
    }
}