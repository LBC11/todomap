package com.example.todomap.profile

import android.Manifest
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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.bumptech.glide.Glide
import com.example.todomap.MainActivity
import com.example.todomap.calendar.CalendarFragment
import com.example.todomap.databinding.FragmentReviseBinding
import com.example.todomap.user.UserAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class ReviseFragment : Fragment() {

    private val TAG: String = "ProfileFragment"

    private lateinit var context: FragmentActivity
    private lateinit var binding: FragmentReviseBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: StorageReference
    private lateinit var database: DatabaseReference

    private val PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1001

    private lateinit var account: UserAccount

    // 갤러리에서 이미지 가져오는 launcher
    val requestLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) binding.reviseImg.setImageURI(it.data?.data)
        }

    private val mainActivity : MainActivity
        get() {
            return activity as MainActivity
        }

    override fun onAttach(activity: Activity) { // Fragment 가 Activity 에 attach 될 때 호출된다.
        context = activity as FragmentActivity
        super.onAttach(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReviseBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()

        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid.toString()
        val email = currentUser?.email.toString()
        account = UserAccount(uid, email, "name", "my info", "-")
        database = FirebaseDatabase.getInstance().reference.child("userAccount").child(uid)
        firebaseStorage = FirebaseStorage.getInstance().reference

        getAccount(uid, email)


        // 프로필 이미지 변경 설정 RequestPermission
        binding.reviseImg.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)
                } else {
                    // 접근 허가된 경우에는 갤러리에서 이미지 가져오기
                    pickImageFromGallery()
                }
            } else {
                pickImageFromGallery()
            }
        }

        // 변경 버튼 누를 시 프로필 사진 storage 에 업로드, 이름 변경
        binding.changeBtn.setOnClickListener {
            account.userName = binding.editUserName.text.toString()
            account.info = binding.editUserDescription.text.toString()
            account.profileImgUrl = account.idToken + "_profileImg"
            database.setValue(account)

            if (binding.reviseImg.drawable != null){
                val imgBitmap = (binding.reviseImg.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                val storageReference = firebaseStorage.child(account.idToken + "_profileImg")

                val uploadTask = storageReference.putBytes(data)
                uploadTask.addOnFailureListener {
                    Log.d(TAG, "Image Upload Failure")
                }.addOnSuccessListener {
                    Log.d(TAG, "Image Upload Success")
                }
            }
            mainActivity.changeFragment(1)

        }

        return binding.root
    }

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
                    binding.editUserName.setText(account.userName)
                    binding.editUserDescription.setText(account.info)

                    // 프로필 사진 받아오기
                    var path: String = firebaseAuth.currentUser?.uid.toString()

                    // 기본 이미지 사용의 경우 path 변경
                    if (account.profileImgUrl.startsWith("basic")) {
                        path = "basic"
                    }
                    firebaseStorage.child(path + "_profileImg").downloadUrl
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Glide.with(context).load(it.result).into(binding.reviseImg)
                                account.profileImgUrl = it.result.toString()
                            } else {
                                Toast.makeText(context, it.exception!!.message, Toast.LENGTH_SHORT).show()
                            }
                        }

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


//    // 프로필 사진 받아오기
//    private fun getImage() {
//        var path: String = firebaseAuth.currentUser?.uid.toString()
//
//        // 기본 이미지 사용의 경우 path 변경
//        if (account.profileImgUrl.startsWith("basic")) {
//            path = "basic"
//        }
//        firebaseStorage.child(path + "_profileImg").downloadUrl
//            .addOnCompleteListener {
//                if (it.isSuccessful) {
//                    Glide.with(this).load(it.result).into(binding.reviseImg)
//                    account.profileImgUrl = it.result.toString()
//                } else {
//                    Toast.makeText(context, it.exception!!.message, Toast.LENGTH_SHORT).show()
//                }
//            }
//    }

}