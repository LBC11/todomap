package com.example.todomap

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.example.todomap.databinding.ActivityMainBinding
import com.example.todomap.user.UserAccount
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private val TAG: String = "ITM"
    private val binding by lazy {ActivityMainBinding.inflate(layoutInflater)}
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val permissionId = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        database = FirebaseDatabase.getInstance().reference

        val uid = currentUser?.uid.toString()
        val email = currentUser?.email.toString()
        saveUserData(uid, email)

        val viewpagerAdapter = ViewPagerFragmentAdapter(this)
        binding.viewPager.adapter = viewpagerAdapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.customView = getTabView(position)
        }.attach()
    }

    private fun getTabView(position: Int): View {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.tab_navigator_item, null, false)
        val ivTab = view.findViewById<ImageView>(R.id.ivTab)

        when (position) {
            0 -> ivTab.setImageResource(R.drawable.navigator_profile)
            1 -> ivTab.setImageResource(R.drawable.navigator_todo)
//            else -> ivTab.setImageResource(R.drawable.navigator_map)
        }
        return view
    }

    private fun saveUserData(uid: String, email: String) {
        val account = UserAccount(uid, email,"-","my info","-")

        database.child("userAccount").child(uid).get()
            .addOnSuccessListener {
                try {
                    Log.d(TAG, "Data about the user is already stored in the db.")
                } catch (e: java.lang.NullPointerException) {
                    database.child("userAccount").child(uid).setValue(account)
                    Log.d(TAG, "save user data in DB")
                }
            }.addOnFailureListener{
                Log.d(TAG, "RealtimeDB get error in the loadAccount")
            }

    }

//    @SuppressLint("MissingPermission", "SetTextI18n")
//    private fun getLocatePermission() {
//        if (checkPermissions()) {
//            if (!isLocationEnabled()) {
//                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
//                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                startActivity(intent)
//            }
//        } else {
//            requestPermissions()
//        }
//    }
//    private fun isLocationEnabled(): Boolean {
//        val locationManager: LocationManager =
//            getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
//            LocationManager.NETWORK_PROVIDER
//        )
//    }
//    private fun checkPermissions(): Boolean {
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            return true
//        }
//        return false
//    }
//    private fun requestPermissions() {
//        ActivityCompat.requestPermissions(
//            this,
//            arrayOf(
//                android.Manifest.permission.ACCESS_COARSE_LOCATION,
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ),
//            permissionId
//        )
//    }
//    @SuppressLint("MissingSuperCall")
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        if (requestCode == permissionId) {
//            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                getLocatePermission()
//            }
//        }
//    }
}