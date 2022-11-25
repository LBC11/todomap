package com.example.todomap.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.todomap.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.reigisterFinishBtn.setOnClickListener {
            val email = binding.newIdText.text.toString()
            val pass = binding.newPasswordText.text.toString()
            val confirmPass = binding.confirmPasswordText.text.toString()

            joinMember(email, pass, confirmPass)
        }
    }

    private fun joinMember(email: String, pass: String, confirmPass: String) {
        if(email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
            if(pass == confirmPass) {
                firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if(it.isSuccessful) {

                        val firebaseUser: FirebaseUser? = firebaseAuth.currentUser
//                        val account = UserAccount()
//                        account.idToken = firebaseUser?.uid
//                        account.emailId = email
//                        account.password = pass
//
//                        database.child("UserAccount")
//                            .child(firebaseUser?.uid ?: "-1")
//                            .setValue(account)
//
//                        Toast.makeText(this, "Succeed to register the account!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, SigninActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
            }
        }  else {
            Toast.makeText(this, "Empty Fields are not allowed !!", Toast.LENGTH_SHORT).show()
        }
    }
}