package com.example.todomap

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.todomap.calendar.CalendarFragment
import com.example.todomap.databinding.ActivityMainBinding
import com.example.todomap.profile.ProfileFragment
import com.example.todomap.profile.UsersearchFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private val TAG: String = "ITM"
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // to override backSpace
    private val finishTime: Long = 1000
    private var pressTime: Long = 0

    // for getting current user location
    private val requestCode = 2000
    private var currentLocation: Location? = null
    lateinit var locationManager: LocationManager

    val fragmentManager = supportFragmentManager
    var profileFrag = ProfileFragment()
    var mapFrag = MapFragment()
    var todoFrag = CalendarFragment()
    var searchFrag = UsersearchFragment()
    var reviseFrag = ReviseFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid.toString()

        //
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        database = FirebaseDatabase.getInstance().reference.child("location").child(uid)

        lifecycleScope.launch {
            while(true) {
                delay(30000)
                getCurrentLocation(hasGps)
            }
        }

        fragmentManager.commit {
            add(binding.fragmentContainer.id, todoFrag)
        }

        binding.profileBtn.setOnClickListener {
            changeFragment(1)
        }
        binding.mapBtn.setOnClickListener {
            changeFragment(2)
        }
        binding.todoBtn.setOnClickListener {
            changeFragment(3)
        }
        binding.searchBtn.setOnClickListener {
            changeFragment(4)
        }
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

    // To prevent user from entering main activity without login
//    @Deprecated("Deprecated in Java")
//    override fun onBackPressed() {
//        val now = System.currentTimeMillis()
//        val intervalTime = now - pressTime
//
//        if (intervalTime in 0..finishTime) {
//            moveTaskToBack(true) // 태스크를 백그라운드로 이동
//            finishAndRemoveTask() // 액티비티 종료 + 태스크 리스트에서 지우기
//
//            exitProcess(0)
//        } else {
//            pressTime = now
//            Toast.makeText(applicationContext, "한번더 누르시면 앱이 종료됩니다", Toast.LENGTH_SHORT).show()
//        }
//    }

    // Get the current user's location
    private fun getCurrentLocation(hasGps: Boolean) {
        requestLocation(hasGps)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                requestCode
            )
            false
        } else {
            true
        }
        val lastKnownLocationByGps =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        Log.d(TAG, lastKnownLocationByGps.toString())
        lastKnownLocationByGps?.let {
            currentLocation = lastKnownLocationByGps
        }

        database.setValue(lastKnownLocationByGps?.let { LatLng(it.latitude, it.longitude) })
            .addOnCompleteListener {
                Log.d(TAG, "success to save the location data")
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to save the location data")
            }

    }

    private fun gpsLocationListener(): LocationListener {
        return object : LocationListener {
            override fun onLocationChanged(location: Location) {
                currentLocation = location
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
    }

    private fun requestLocation(hasGps: Boolean) {
        if (hasGps) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    requestCode
                )
                false
            } else {
                true
            }
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                0F,
                gpsLocationListener()
            )
            Log.d(TAG, "location request success")
        }
    }

    fun changeFragment(index: Int){
        when(index){
            1 -> {
                supportFragmentManager
                    .beginTransaction()
                    .replace(binding.viewPager.id, fragment_username)
                    .commit()
            }
        }
    }
}