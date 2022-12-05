package com.example.todomap.profile

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.todomap.databinding.UseritemRecyclerBinding
import com.example.todomap.user.UserAccount
import com.example.todomap.profile.UsersearchFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UsersearchAdapter(var context: UsersearchFragment) : RecyclerView.Adapter<UsersearchAdapter.ViewHolder>(), Filterable {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var userFriendRef: DatabaseReference
    private lateinit var allUserRef: DatabaseReference

    var allUsers: ArrayList<UserAccount> = arrayListOf()

    private var users = ArrayList<UserAccount>()
    private var filteredUser = ArrayList<UserAccount>()
    private var itemFilter = ItemFilter()
    val TAG = "Usersearch"

    //filteredUser에 추가
    init {
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid.toString()

        database = FirebaseDatabase.getInstance().reference
        userFriendRef = database.child("friend").child(uid)
        allUserRef = database.child("userAccount")

        // 모든 유저의 uid 가져오기
        getallUserLists()
//        users.addAll(context.allUsers)
//        filteredUser.addAll(context.allUsers)
//        Log.d(TAG, " Adapter: $filteredUser")
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersearchAdapter.ViewHolder {
        val binding = UseritemRecyclerBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, " Adapter: filteredUserposition ${filteredUser[position]}")
        holder.setUI(filteredUser[position])
    }

    override fun getItemCount(): Int {
        return filteredUser.size
    }


    inner class ViewHolder(private val binding: UseritemRecyclerBinding): RecyclerView.ViewHolder(binding.root){
        fun setUI(user: UserAccount){
            // user 정보 띄우기
            Log.d(TAG, " Adapter: setUI ${user.userName}")
            binding.userName.text = user.userName
//            binding.profileImg.setImageURI(Uri.parse(user.profileImgUrl))
            binding.root.setOnClickListener {
                // user 클릭하면 뭘 할건지
                // Dialog
            }
        }
    }

    override fun getFilter(): Filter {
        return itemFilter
    }

    inner class ItemFilter: Filter(){

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterString = constraint.toString()
            val results = FilterResults()
            Log.d(TAG, "charSequence : $constraint")

            //검색이 필요없을 경우를 위해 원본 배열을 복제
            val filteredList: ArrayList<UserAccount> = ArrayList()

            if (filterString.trim { it <= ' ' }.isEmpty()) {
                //공백제외 아무런 값이 없을 경우 -> 원본 배열
                results.values = allUsers
                results.count = allUsers.size
                Log.d(TAG, " Adapter: results ${results.values}")
                return results
            } else if (filterString.trim { it <= ' ' }.length >= 2) {
                Log.d(TAG, " Adapter: filteredString ${filterString.trim { it <= ' ' }}")
                //공백 제외 2글자이상인 경우 검색
                for (user in allUsers) {
                    if (user.userName.contains(filterString)) {
                        filteredList.add(user)
                        Log.d(TAG, " Adapter: filteredList $filteredList")
                    }
                }
            }
            results.values = filteredList
            results.count = filteredList.size

            return results
        }
        
        // 결과 보여주깅
        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredUser.clear()
            filteredUser.addAll(results?.values as ArrayList<UserAccount>)
            Log.d(TAG, " Adapter: publish filteredUser ${results?.values}")
            Log.d(TAG, " Adapter: publish filteredUser $filteredUser")
            notifyDataSetChanged()
        }

    }

    private fun getallUserLists(){
        allUserRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "success to load userAccount in DB")

                    snapshot.children?.forEach{
//                        Log.d(TAG, it.toString())
                        val hash = it.value as HashMap<*, *>?
//                        Log.d(TAG, hash?.get("idToken").toString())

//                        allUserUids.add(hash?.get("idToken").toString())
                        allUsers.add(UserAccount(hash?.get("idToken").toString(),
                            hash?.get("email").toString(),
                            hash?.get("userName").toString(),
                            hash?.get("info").toString(),
                            hash?.get("profileImgUrl").toString(),
                        )
                        )
                    }

                    Log.d(TAG, "Fragment: $allUsers")
//                    Log.d(TAG, "Fragment: $allUserUids")

                } else {
                    Log.d(TAG, "There are no userAccount in DB")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Failed to load userAccount in DB")
            }
        })
    }

}