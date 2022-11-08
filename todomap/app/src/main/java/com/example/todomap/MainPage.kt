package com.example.todomap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.todomap.databinding.ActivityMainPageBinding
import com.google.firebase.auth.FirebaseAuth

class MainPage : AppCompatActivity() {

    private lateinit var binding: ActivityMainPageBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnLogout.setOnClickListener {
            firebaseAuth.signOut()

            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
        }

        binding.btnDeleteMember.setOnClickListener {
            firebaseAuth.currentUser?.delete()

            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
        }
    }
}