package com.example.todomap.calendar

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.todomap.MainActivity
import com.example.todomap.databinding.FrienditemRecyclerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FriendsAdapter(var context: CalendarFragment): RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {
    private val TAG = "FriendsCalendarAdapter"

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var firebaseStorage: StorageReference = FirebaseStorage.getInstance().reference
    private var userFriendRef: DatabaseReference

    var myUid = ""
    private var friendsUid: ArrayList<String> = arrayListOf()

    private val mainActivity : MainActivity
        get() {
            return context.activity as MainActivity
        }

    init {
        val currentUser = firebaseAuth.currentUser
        myUid = currentUser?.uid.toString()
        userFriendRef = database.child("friend").child(myUid)
//        // 친구의 uid 가져오기
//        getFriendList()
    }

    inner class ViewHolder(private val binding: FrienditemRecyclerBinding): RecyclerView.ViewHolder(binding.root){
        fun setUI(friendUid: String){
            Log.d(TAG, " Into SetUI")
            firebaseStorage.child(friendUid + "_profileImg").downloadUrl
                .addOnCompleteListener {
                    Log.d(TAG, " Adapter: img URI ${it.result}")
                    if (it.isSuccessful) {
                        Glide.with(context).load(it.result).into(binding.friendImg)
                    } else {
                        Toast.makeText(context as FragmentActivity, it.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }

            // 친구 이미지 클릭하면
            binding.friendImg.setOnClickListener {
                mainActivity.changeFragment(5, friendUid)
            }

        }
    }

    //친구 목록 CalendarFragment에서 사용
    fun getFriendList(){
        userFriendRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    //친구 목록 리스트 생성
                    Log.d(TAG, "Successful Access in friends DB")
                    snapshot.children.forEach{
                        friendsUid.add(it.value.toString())
                    }
                    Log.d(TAG, "get FriendsUid ${friendsUid}")
                    notifyDataSetChanged()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Failed to load friend DB")
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FrienditemRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setUI(friendsUid[position])
    }

    override fun getItemCount(): Int {
        return friendsUid.size
    }
}