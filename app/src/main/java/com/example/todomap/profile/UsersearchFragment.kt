package com.example.todomap.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todomap.databinding.FragmentUsersearchBinding
import com.example.todomap.user.UserAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.StorageReference

class UsersearchFragment : Fragment() {

    private val TAG: String = "UsersearchFragment"

    private lateinit var binding: FragmentUsersearchBinding
    lateinit var adapter: UsersearchAdapter
    lateinit var users: ArrayList<UserAccount>

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: StorageReference
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUsersearchBinding.inflate(inflater, container, false)

        // user 정보 가져오기
        users = callUserAccounts()

        // Realtime DB에서 결과 가져오기gh


        // Adapter
        adapter = UsersearchAdapter(users, this)
        binding.userRecyclerView.layoutManager = LinearLayoutManager(context)
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

        return binding.root
    }

    fun callUserAccounts(): ArrayList<UserAccount> {
        var userList: ArrayList<UserAccount>
        userList = ArrayList<UserAccount>(3)
        database = FirebaseDatabase.getInstance().reference.child("userAccount")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "success to load userAccount in DB")
                    val tempHash = snapshot.value as HashMap<*, *>?
                    tempHash?.forEach {
                        var tmp = UserAccount()
                        Log.d(TAG, it.toString())
//                        tmp.userName = it.get("info").toString()
//                        account.profileImgUrl = tempHash?.get("profileImgUrl").toString()
//                        account.userName = tempHash?.get("userName").toString()
//
//                        // email, username, info 설정
//                        binding.userEmailText.text = account.email
//                        binding.userNameText.setText(account.userName)
//                        binding.info.setText(account.info)
                    }

                } else {
                    Log.d(TAG, "There are no userAccount in DB")
//                    val account = UserAccount(uid, email,"-","my info","basic")
//                    database.setValue(account)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Failed to load userAccount in DB")
            }
        })


        return userList
    }
}