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

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var userFriendRef: DatabaseReference
    private lateinit var allUserRef: DatabaseReference

    private var allUserUids: MutableList<String> = arrayListOf()
    lateinit var users: ArrayList<UserAccount>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUsersearchBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid.toString()
        val email = currentUser?.email.toString()

        database = FirebaseDatabase.getInstance().reference
        userFriendRef = database.child("friend").child(uid)
        allUserRef = database.child("userAccount")

        // 모든 유저의 uid 가져오기
        allUserUids = getallUserLists()




        // Adapter > 검색할 때 모든 유저의 account 정보 넘겨줘야 함
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

    private fun getallUserLists(): MutableList<String> {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "success to load userAccount in DB")
                    val hash = snapshot.value as HashMap<*, *>?

//                    유저 정보 변경 관련 event listner
//                    if (allUserUids.isNotEmpty()) {
//                        allUserUids.forEach {
//                            // remove the existing listener
//                            allUserRef.child(it)
//                                .removeEventListener(accountListenerHashMap[it]!!)
//                            friendsLocationRef.child(it)
//                                .removeEventListener(locationListenerHashMap[it]!!)
//
//                            // clear the hash map
//                            accountListenerHashMap.clear()
//                            locationListenerHashMap.clear()
//                        }

                    hash?.forEach {
                        allUserUids.add(it.value.toString())
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

        return allUserUids
    }
}