package com.example.todomap

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.todomap.user.UserAccount
import com.example.todomap.profile.ProfileFragment
import com.example.todomap.calendar.CalendarFragment

class ViewPagerFragmentAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    private val TYPE_PROFILE = 0
    private val TYPE_TODO = 1
    private val TYPE_MAP = 2
    private var listType: List<Int> = listOf(TYPE_PROFILE, TYPE_TODO, TYPE_MAP)

    override fun getItemCount(): Int {
        return listType.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            TYPE_PROFILE -> ProfileFragment()
            //TYPE_TODO -> CalendarFragment(uid)
            else -> CalendarFragment()
//            else ->MapFragment()
        }
    }
}