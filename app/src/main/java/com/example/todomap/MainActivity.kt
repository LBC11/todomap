package com.example.todomap

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.example.todomap.calendar.CalendarFragment
import com.example.todomap.databinding.ActivityMainBinding
import com.example.todomap.profile.ProfileFragment
import com.example.todomap.profile.ReviseFragment
import com.example.todomap.profile.UsersearchFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG: String = "MainActivity"
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // for getting current user location
    private var currentLocation: Location? = null
    private var locationByGps: Location? = null
    private var locationByNetwork: Location? = null
    private lateinit var locationManager: LocationManager

    private val fragmentManager = supportFragmentManager
    private var profileFrag = ProfileFragment()
    private var mapFrag = MapFragment()

    private var todoFrag = CalendarFragment()
    private var searchFrag = UsersearchFragment()
    private var reviseFrag = ReviseFragment()

    private var locationPermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid.toString()

        // 현재 위치
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        database = FirebaseDatabase.getInstance().reference.child("location").child(uid)

        lifecycleScope.launch {
            while (true) {
                saveCurrentLocation()
                delay(30000)
            }
        }

        fragmentManager.commit {
            add(binding.fragmentContainer.id, profileFrag)
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

    fun changeFragment(index: Int) {
        when (index) {
            0 -> {
                fragmentManager.commit {
                    setReorderingAllowed(true)
                    addToBackStack(null)
                    replace(binding.fragmentContainer.id, reviseFrag)
                }
            }
            1 -> {
                fragmentManager.commit {
                    setReorderingAllowed(true)
                    addToBackStack(null)
                    replace(binding.fragmentContainer.id, profileFrag)
                }
            }
            2 -> {
                fragmentManager.commit {
                    setReorderingAllowed(true)
                    addToBackStack(null)
                    replace(binding.fragmentContainer.id, mapFrag)
                }
            }
            3 -> {
                fragmentManager.commit {
                    setReorderingAllowed(true)
                    addToBackStack(null)
                    replace(binding.fragmentContainer.id, todoFrag)
                }
            }
            4 -> {
                fragmentManager.commit {
                    setReorderingAllowed(true)
                    addToBackStack(null)
                    replace(binding.fragmentContainer.id, searchFrag)
                }
            }
        }
    }

    // Check location permissions
    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) locationPermissionGranted = true
        else ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) locationPermissionGranted =
                    true
            }
        }
    }

    private fun saveCurrentLocation() {
        requestForLocationByGps()
        requestForLocationByNetwork()

        currentLocation = if (locationByGps != null) {
            locationByGps
        } else {
            locationByNetwork
        }

        if(currentLocation != null) {
            saveLocationToDB()
        }
    }


    private fun saveLocationToDB() {
        database.setValue(currentLocation?.let { LatLng(it.latitude, it.longitude) })
            .addOnCompleteListener {
                Log.d(TAG, "success to save the location data")
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to save the location data")
            }
    }

    private fun requestForLocationByGps() {
        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (hasGps) {
            try {
                if (locationPermissionGranted) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        5000,
                        0F,
                        gpsLocationListener()
                    )
                    Log.d(TAG, "location request by Gps success")

                    val lastKnownLocationByGps =
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    lastKnownLocationByGps?.let {
                        currentLocation = lastKnownLocationByGps
                    }

                } else {
                    getLocationPermission()
                }

            } catch (e: SecurityException) {
                Log.e(TAG, e.message!!)
            }
        } else {
            enableGpsSetting()
        }
    }

    private fun requestForLocationByNetwork() {
        val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasNetwork) {
            try {
                if (locationPermissionGranted) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        5000,
                        0F,
                        networkLocationListener()
                    )
                    Log.d(TAG, "location request by network success")

                    val lastKnownLocationByNetwork =
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    lastKnownLocationByNetwork?.let {
                        locationByNetwork = lastKnownLocationByNetwork
                    }
                } else {
                    getLocationPermission()
                }

            } catch (e: SecurityException) {
                Log.e(TAG, e.message!!)
            }
        } else {
            enableNetworkSetting()
        }
    }

    // Listener for the gps request
    private fun gpsLocationListener(): LocationListener {
        return object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationByGps = location
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
    }

    // Listener for the network request
    private fun networkLocationListener(): LocationListener {
        return object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationByNetwork= location
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
    }

    // request for the turn on the GPS function
    private fun enableGpsSetting() {
        val gpsSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(gpsSettingIntent)
    }

    // request for the turn on the network
    private fun enableNetworkSetting() {
        val networkSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(networkSettingIntent)
    }
}