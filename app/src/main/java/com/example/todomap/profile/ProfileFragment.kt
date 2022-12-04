package com.example.todomap.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.todomap.MainActivity
import com.example.todomap.calendar.TodoViewModel
import com.example.todomap.databinding.FragmentProfileBinding
import com.example.todomap.login.SigninActivity
import com.example.todomap.user.UserAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProfileFragment : Fragment() {

    private val TAG: String = "ProfileFragment"

    private lateinit var context: FragmentActivity
    private lateinit var binding: FragmentProfileBinding

    private val todoViewModel: TodoViewModel by viewModels()

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var firebaseStorage: StorageReference
    private lateinit var database: DatabaseReference

    private val PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1001

    private lateinit var account: UserAccount

    // 갤러리에서 이미지 가져오는 launcher
    private val requestLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) binding.profileMyImg.setImageURI(it.data?.data)
        }

    @Deprecated("Deprecated in Java")
    override fun onAttach(activity: Activity) { // Fragment 가 Activity 에 attach 될 때 호출된다.
        context = activity as FragmentActivity
        super.onAttach(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.profileMyImg.clipToOutline = true

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid.toString()
        val email = currentUser?.email.toString()

        database = FirebaseDatabase.getInstance().reference.child("userAccount").child(uid)
        firebaseStorage = FirebaseStorage.getInstance().reference

        account = UserAccount(uid, email, "name", "my info", "-")
        // Account 정보 호출
        getAccount(uid, email)

        // 프로필 변경
        binding.profileView.setOnClickListener {
            activity.changeFragment(1)
        }

        //로그아웃
        binding.signoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(context, SigninActivity::class.java)

            // To prevent user from entering main activity without login
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            startActivity(intent)
        }
        //회원 탈퇴
        binding.withdrawBtn.setOnClickListener {
            // delete the user image in the storage
            if(account.profileImgUrl != "basic") {
                firebaseStorage.child(account.idToken + "_profileImg").delete()
                account.profileImgUrl = "basic"
            }
            // delete the user data in the realtime DB
            database.removeValue()
            // delete the user todoList

            // delete the user account
            firebaseAuth.currentUser?.delete()
            // Go to the SignIn activity
            val intent = Intent(context, SigninActivity::class.java)

            // To prevent user from entering main activity without login
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            startActivity(intent)
        }
        return binding.root
    }


    // realtimeDB 에서 account 가져오기
    private fun getAccount(uid: String, email: String) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "success to load userAccount in DB")
                    val tempHash = snapshot.value as HashMap<*, *>?
                    account.info = tempHash?.get("info").toString()
                    account.profileImgUrl = tempHash?.get("profileImgUrl").toString()
                    account.userName = tempHash?.get("userName").toString()

                    // email, username, info 설정
                    binding.userEmailText.text = account.email
                    binding.userNameText.text = account.userName

                    // 프로필 사진 받아오기
                    getImage()

                } else {
                    Log.d(TAG, "There are no userAccount in DB")
                    val account = UserAccount(uid, email,"-","my info","basic")
                    database.setValue(account)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Failed to load userAccount in DB")
            }
        })
    }

    // 프로필 사진 받아오기
    private fun getImage() {
        var path: String = firebaseAuth.currentUser?.uid.toString()

        // 기본 이미지 사용의 경우 path 변경
        if (account.profileImgUrl.startsWith("basic")) {
            path = "basic"
        }

        firebaseStorage.child(path + "_profileImg").downloadUrl
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Glide.with(this).load(it.result).into(binding.profileMyImg)
                    account.profileImgUrl = it.result.toString()
                } else {
                    Toast.makeText(context, it.exception!!.message, Toast.LENGTH_SHORT).show()
                }
            }
    }
}


//        binding.buttonTemp.setOnClickListener {
//            val imgBitmap = (binding.profileImg.drawable as BitmapDrawable).bitmap
//            val baos = ByteArrayOutputStream()
//            imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//            val data = baos.toByteArray()
//            val storageReference = firebaseStorage.child("basic" + "_profileImg")
//
//            val uploadTask = storageReference.putBytes(data)
//            uploadTask.addOnFailureListener {
//                Log.d("ITM", "Image Upload Failure")
//            }.addOnSuccessListener {
//                Log.d("ITM", "Image Upload Success")
//            }
//        }

//        binding.buttonTemp.setOnClickListener {
//            lifecycleScope.launch(Dispatchers.IO) {
//                val temp = RetrofitService.todoService.getTodos("uid1")
//                Log.d("ITM", "${temp}")
//            }
//        }