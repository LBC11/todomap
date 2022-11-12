package com.example.todomap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todomap.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar

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
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()

        transaction.add(binding.fragmentFrame.id, calendarFragment)
        transaction.commit()

    }

}