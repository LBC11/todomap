package com.example.todomap.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todomap.databinding.FragmentUsersearchBinding

class UsersearchFragment : Fragment() {

    private lateinit var binding: FragmentUsersearchBinding
    lateinit var adapter: UsersearchAdapter
    lateinit var layoutManager: LinearLayoutManager

    private var allUserUids: MutableList<String> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUsersearchBinding.inflate(inflater, container, false)

        // Adapter > 검색할 때 모든 유저의 account 정보 넘겨줘야 함
        adapter = UsersearchAdapter(this)
        binding.userRecyclerView.adapter = adapter

        layoutManager = LinearLayoutManager(context)
        binding.userRecyclerView.layoutManager = layoutManager

//        Log.d(TAG, "Fragment${allUsers.toString()}")

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

        return binding.root
    }


}