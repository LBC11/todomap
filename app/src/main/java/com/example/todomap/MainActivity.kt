package com.example.todomap

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.example.todomap.databinding.ActivityMainBinding
import com.example.todomap.profile.UserAccount
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private val binding by lazy {ActivityMainBinding.inflate(layoutInflater)}
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val userIntent = intent

//        uid 어떻게 할건지 구현 필요!!!!!!!!
        val uid = ""
        val viewpagerAdapter = ViewPagerFragmentAdapter(this, uid, userIntent.getSerializableExtra("UserAccount") as UserAccount?)
        binding.viewPager.adapter = viewpagerAdapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.customView = getTabView(position)
        }.attach()
    }

    fun getTabView(position: Int): View {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.tab_navigator_item, null, false)
        val ivTab = view.findViewById<ImageView>(R.id.ivTab)

        when (position) {
            0 -> ivTab.setImageResource(R.drawable.navigator_profile)
            1 -> ivTab.setImageResource(R.drawable.navigator_todo)
            else -> ivTab.setImageResource(R.drawable.navigator_map)
        }

        return view
    }

}