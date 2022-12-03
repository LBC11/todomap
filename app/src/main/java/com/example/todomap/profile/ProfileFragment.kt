package com.example.todomap.profile

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.todomap.calendar.TodoViewModel
import com.example.todomap.databinding.FragmentProfileBinding
import com.example.todomap.login.SigninActivity
import com.example.todomap.retrofit.service.RetrofitService
import com.example.todomap.user.UserAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

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
            if (it.resultCode == Activity.RESULT_OK) binding.profileImg.setImageURI(it.data?.data)
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
        binding.profileImg.clipToOutline = true

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid.toString()
        val email = currentUser?.email.toString()

        database = FirebaseDatabase.getInstance().reference.child("userAccount").child(uid)
        firebaseStorage = FirebaseStorage.getInstance().reference

        account = UserAccount(uid, email, "name", "my info", "-")

        getAccount(uid, email)

        // 프로필 이미지 설정
        binding.profileImg.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_DENIED
                ) {
                    ActivityCompat.requestPermissions(
                        context,
                        arrayOf(READ_EXTERNAL_STORAGE),
                        PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                    )
                } else {
                    pickImageFromGallery()
                }
            } else {
                pickImageFromGallery()
            }
        }

        // 프로필 변경 버튼 누를 시 프로필 사진 storage 에 업로드, 이름 변경
        binding.profileChangeBtn.setOnClickListener {
            account.userName = binding.userNameText.text.toString()
            account.info = binding.userInfoText.text.toString()
            account.profileImgUrl = account.idToken + "_profileImg"
            database.setValue(account)

            val imgBitmap = (binding.profileImg.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val storageReference = firebaseStorage.child(account.idToken + "_profileImg")

            val uploadTask = storageReference.putBytes(data)
            uploadTask.addOnFailureListener {
                Log.d("ITM", "Image Upload Failure")
            }.addOnSuccessListener {
                Log.d("ITM", "Image Upload Success")
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

        //로그아웃
        binding.signoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(context, SigninActivity::class.java)
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
                    binding.userNameText.setText(account.userName)
                    binding.userInfoText.setText(account.info)

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
                    Glide.with(this).load(it.result).into(binding.profileImg)
                    account.profileImgUrl = it.result.toString()
                } else {
                    Toast.makeText(context, it.exception!!.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    // 갤러리에서 인텐트로 이미지 가져오기
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        requestLauncher.launch(intent)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery()
                } else Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}