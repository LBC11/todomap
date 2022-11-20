package com.example.todomap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.todomap.databinding.ActivityMainBinding
import com.example.todomap.login.SigninActivity
import com.example.todomap.login.SignupActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private val binding by lazy {ActivityMainBinding.inflate(layoutInflater)}
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.signoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, SigninActivity::class.java)
            startActivity(intent)
        }

        binding.withdrawBtn.setOnClickListener {
            firebaseAuth.currentUser?.delete()
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        var calendarFragment = CalendarFragment()
        var mapFragment = MapFragment()
        val fragments = listOf(calendarFragment, mapFragment)
        val fragmentAdapter = FragmentAdapter(this)
        fragmentAdapter.fragmentList = fragments
        binding.viewpager.adapter = fragmentAdapter


    }

}