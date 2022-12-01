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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.todomap.calendar.TodoViewModel
import com.example.todomap.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.bumptech.glide.Glide
import com.example.todomap.login.SigninActivity
import com.example.todomap.login.SignupActivity
import com.example.todomap.retrofit.service.RetrofitService
import com.example.todomap.user.UserAccount
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {

    private val TAG: String = "ITM"

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
        database = FirebaseDatabase.getInstance().reference
        firebaseStorage = FirebaseStorage.getInstance().reference

        // email, username 받아오기
        binding.userEmailText.text = account.email
        binding.userNameText.setText(account.userName)
        binding.info.setText(account.infor)
        Log.d(TAG, "${account.email}, ${account.userName}, ${account.infor}")

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
            account.userName = binding.userNameText.toString()
            account.profileImgUrl = account.idToken + "_profileImg"
            database.child("userAccount").child(account.idToken).setValue(account)

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

        // 기본 이미지가 아닐 때 프로필 사진 받아오기
        if (account.profileImgUrl != "-") {
            firebaseStorage.child(firebaseAuth.currentUser?.uid.toString() + "_profileImg").downloadUrl
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Glide.with(this).load(it.result).into(binding.profileImg)
                        account.profileImgUrl = it.result.toString()
                    } else {
                        Toast.makeText(context, it.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.buttonTemp.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val temp = RetrofitService.todoService.getTodos("uid1")
                Log.d("ITM", "${temp}")
            }
        }

        //로그아웃
        binding.signoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(context, SigninActivity::class.java)
            startActivity(intent)
        }
        //회원 탈퇴
        binding.withdrawBtn.setOnClickListener {
            firebaseAuth.currentUser?.delete()
            val intent = Intent(context, SigninActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    // 갤러리에서 인텐트로 이미지 가져오기
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        requestLauncher.launch(intent)
    }

    private fun getAccount(uid: String, email: String) {
        database.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

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