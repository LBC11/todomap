package com.example.todomap.profile

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.todomap.databinding.UseritemRecyclerBinding
import com.example.todomap.user.UserAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UsersearchAdapter(var context: UsersearchFragment) : RecyclerView.Adapter<UsersearchAdapter.ViewHolder>(), Filterable {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var firebaseStorage: StorageReference
    private lateinit var userFriendRef: DatabaseReference
    private lateinit var allUserRef: DatabaseReference

    var allUsers: ArrayList<UserAccount> = arrayListOf()
    var myUid = ""

    private var friendsUid : ArrayList<String> = arrayListOf()
    private var filteredUser = ArrayList<UserAccount>()
    private var itemFilter = ItemFilter()
    val TAG = "Usersearch"

    //filteredUser에 추가
    init {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance().reference
        val currentUser = firebaseAuth.currentUser
        myUid = currentUser?.uid.toString()

        database = FirebaseDatabase.getInstance().reference
        userFriendRef = database.child("friend").child(myUid)
        allUserRef = database.child("userAccount")

        // 모든 유저의 uid 가져오기
        getallUserLists()
//        users.addAll(context.allUsers)
//        filteredUser.addAll(context.allUsers)
//        Log.d(TAG, " Adapter: $filteredUser")
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersearchAdapter.ViewHolder {
        val binding = UseritemRecyclerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
            binding.userInfo.text = user.info
            firebaseStorage.child(user.idToken + "_profileImg").downloadUrl
                .addOnCompleteListener {
                    Log.d(TAG, " Adapter: URI ${it.result}")
                    if (it.isSuccessful) {
                        Glide.with(context).load(it.result).into(binding.profileImg)
                    } else {
                        Toast.makeText(context as FragmentActivity, it.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
            binding.view27.setOnClickListener {
                friendsUid.clear()
                // user 클릭하면 뭘 할건지
                AlertDialog.Builder(binding.root.context)
                    .setTitle("Invite Friend")
                    .setMessage("Do you want to invite ${user.userName}?")
                    .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, which ->
                        userFriendRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()){
                                    Log.d(TAG, "Successful Access in friends DB")
                                    //친구 목록 리스트 생성
                                    snapshot.children.forEach{
//                                        val hash = it.value as HashMap<*, *>?
                                        friendsUid.add(it.value.toString())
                                    }
                                    Log.d(TAG, "FriendsUid ${friendsUid}")
                                    //친구추가
                                    if (friendsUid.contains(user.idToken)){
                                        // 이미 친구일 때
                                        Toast.makeText(binding.root.context, "Already friend with ${user.userName}!", Toast.LENGTH_SHORT).show()
                                    } else if (user.idToken == myUid){
                                        Toast.makeText(binding.root.context, "Can't be friend with myself!", Toast.LENGTH_SHORT).show()
                                    }
                                    else{
                                        // 친구 추가
//                                        friendsUid.clear()
                                        friendsUid.add(user.idToken)
                                        userFriendRef.setValue(friendsUid)
                                        Toast.makeText(binding.root.context, "Add ${user.userName} to new friend!", Toast.LENGTH_SHORT).show()
                                    }

                                } else {
                                    // Add new friends
                                    Log.d(TAG, "There are no friend in DB")
                                    friendsUid.add(user.idToken)
                                    userFriendRef.setValue(friendsUid)
                                    Toast.makeText(binding.root.context, "Add ${user.userName} to new friend!", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.d(TAG, "Failed to load friend DB")
                            }
                        })
                    })
                    .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
//                        Toast.makeText(context as FragmentActivity, "What?????", Toast.LENGTH_SHORT).show()
                    })
                    .show()

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