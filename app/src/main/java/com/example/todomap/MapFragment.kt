package com.example.todomap

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.todomap.calendar.TodoViewModel
import com.example.todomap.databinding.FragmentMapBinding
import com.example.todomap.retrofit.model.TodoEntity
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private const val TAG = "MapFragment"
        private const val DEFAULT_ZOOM = 15F
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
    }

    private lateinit var context: FragmentActivity
    private lateinit var binding: FragmentMapBinding

    private val todoViewModel: TodoViewModel by viewModels()

    // map object
    private lateinit var map: GoogleMap
    private lateinit var mapView: MapView

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private var currentMarker: Marker? = null
    private var currentLatlng: LatLng? = null
    private var cameraPosition: Location? = null

    // defaultLocation: Seoul
    private val defaultLocation = LatLng(37.56, 126.97)

    private var locationPermissionGranted = false

    private var friendsUid: MutableList<String> = arrayListOf()
    private var locationListenerHashMap = HashMap<String, ValueEventListener>()
    private var accountListenerHashMap = HashMap<String, ValueEventListener>()
    private var todoList: MutableList<TodoEntity> = arrayListOf()


    private val friendMarkerHashMap = HashMap<String, Marker>()
    private val todoMarkerHashMap = HashMap<String, Marker>()

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var database: DatabaseReference

    private lateinit var userLocationRef: DatabaseReference
    private lateinit var friendRef: DatabaseReference
    private lateinit var locationRef: DatabaseReference
    private lateinit var accountRef: DatabaseReference

    private var flag = MutableLiveData<Boolean>()

    override fun onAttach(activity: Activity) { // Fragment ??? Activity ??? attach ??? ??? ????????????.
        context = activity as FragmentActivity
        super.onAttach(activity)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        savedInstanceState?.let {
            currentLatlng = it.getParcelable(KEY_LOCATION)
            cameraPosition = it.getParcelable(KEY_CAMERA_POSITION)
        }
        binding = FragmentMapBinding.inflate(inflater, container, false)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)

        binding.mapView.getMapAsync(this)

        binding.myLocaBtn.setOnClickListener {
            if (currentLatlng != null) {
                val cameraUpdate = CameraUpdateFactory.newLatLng(currentLatlng!!)
                map.moveCamera(cameraUpdate)
            }
        }

        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser!!
        val uid = currentUser.uid.toString()

        database = FirebaseDatabase.getInstance().reference
        userLocationRef = database.child("location").child(uid)
        friendRef = database.child("friend").child(uid)
        locationRef = database.child("location")
        accountRef = database.child("userAccount")

        lifecycleScope.launch(Dispatchers.IO) {

            if(todoMarkerHashMap.isNotEmpty()) {
                todoMarkerHashMap.forEach {
                    it.value.remove()
                }
                todoMarkerHashMap.clear()
            }

            todoViewModel.getAllByDate(uid, getCurrentDate()).forEach {
                todoList.add(it)
            }
        }

        flag.observe(viewLifecycleOwner) {
            if(it == false) {
                todoList.forEach {
                    val tempLatlng = LatLng(it.locLatitude, it.locLongitude)
                    setTodoLocationMarker(it.id.toString(), tempLatlng , it.locName)
                }
                flag.postValue(true)
            }
        }

        return binding.root
    }

    private fun getCurrentDate(): String{
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Calendar.getInstance().time)
    }

    private fun getFriendList() {
        friendRef.addValueEventListener(friendListListener())
    }

    private fun friendListListener() = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                val hash = snapshot.value as ArrayList<*>?
                if (friendsUid.isNotEmpty()) {
                    friendsUid.forEach {
                        // remove the existing listener
                        accountRef.child(it)
                            .removeEventListener(accountListenerHashMap[it]!!)
                        locationRef.child(it)
                            .removeEventListener(locationListenerHashMap[it]!!)

                        // clear the hash map
                        accountListenerHashMap.clear()
                        locationListenerHashMap.clear()
                    }

                    friendsUid.clear()
                }

                hash?.forEach {
                    friendsUid.add(it.toString())
                }

                getFriendAccount()

            } else {
                Log.d(TAG, "There are no user's friends data in DB")
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.d(TAG, "Failed to load friend list in DB")
        }
    }

    private fun getFriendAccount() {
        if (friendsUid.isNotEmpty()) {
            friendsUid.forEach {
                val listener = friendAccountListener(it)
                accountListenerHashMap[it] = listener
                accountRef.child(it).addValueEventListener(listener)
            }
        } else {
            Log.d(TAG, "There are no friend.")
        }
    }

    private fun getFriendLocation(uid: String, userName: String, info: String) {
        val listener = friendLocationListener(uid, userName, info)
        locationListenerHashMap[uid] = listener
        locationRef.child(uid).addValueEventListener(listener)
    }

    private fun friendAccountListener(uid: String) = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                Log.d(TAG, "success to load friend's account in DB")

                if (locationListenerHashMap.containsKey(uid)) {
                    locationRef.child(uid)
                        .removeEventListener(locationListenerHashMap[uid]!!)
                }

                val hash = snapshot.value as HashMap<*, *>?
                getFriendLocation(
                    uid,
                    hash?.get("userName").toString(),
                    hash?.get("info").toString()
                )

            } else {
                Log.d(TAG, "There are no friend's location in DB")
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.d(TAG, "Failed to load friend's location in DB")
        }
    }

    private fun friendLocationListener(uid: String, userName: String, info: String) =
        object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "success to load friend's location in DB")

                    if (friendMarkerHashMap.containsKey(uid)) {
                        // ????????? marker ??????
                        friendMarkerHashMap[uid]?.remove()
                        // ????????? marker data ??????
                        friendMarkerHashMap.remove(uid)
                    }

                    // ????????? ?????? data ??????
                    val hash = snapshot.value as HashMap<*, *>?
                    val latlng = LatLng(
                        hash?.get("latitude") as Double,
                        hash["longitude"] as Double
                    )

                    // Generate the friend's marker
                    setFriendLocationMarker(uid, latlng, userName, info)

                } else {
                    Log.d(TAG, "There are no friend's location in DB")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Failed to load friend's location in DB")
            }
        }

    private fun getUserAccount(uid: String) {
        accountRef.child(uid).addValueEventListener(userAccountListener(uid))
    }

    private fun userAccountListener(uid: String) = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                Log.d(TAG, "success to load account in DB")
                if (locationListenerHashMap.containsKey(uid)) {
                    locationRef.child(uid)
                        .removeEventListener(locationListenerHashMap[uid]!!)
                }

                val hash = snapshot.value as HashMap<*, *>?
                getUserLocation(
                    uid,
                    hash?.get("userName").toString(),
                    hash?.get("info").toString()
                )

            } else {
                Log.d(TAG, "There are no account data in DB")
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.d(TAG, "Failed to load friend's location in DB")
        }
    }

    private fun getUserLocation(uid: String, userName: String, info: String) {
        val listener = userLocationListener(userName, info)
        locationListenerHashMap[uid] = listener
        locationRef.child(uid).addValueEventListener(listener)
    }

    private fun userLocationListener(userName: String, info: String) = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                Log.d(TAG, "success to load user's location in DB")
                val hash = snapshot.value as HashMap<*, *>?
                val latlng = LatLng(
                    hash?.get("latitude") as Double,
                    hash["longitude"] as Double
                )
                currentLatlng = latlng
                setUserLocationMarker(latlng, userName, info)

            } else {
                Log.d(TAG, "There are no user's location in DB")
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.d(TAG, "Failed to load friend list in DB")
        }
    }

    private fun setFriendLocationMarker(uid: String, latlng: LatLng, markerTitle: String, markerSnippet: String) {
        // Setting the marker for default location
        val markerOptions = MarkerOptions()
        markerOptions.position(latlng)
        markerOptions.title(markerTitle)
        markerOptions.snippet(markerSnippet)
        markerOptions.draggable(true)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))

        // add the marker to map
        friendMarkerHashMap[uid] = map.addMarker(markerOptions)!!
    }

    private fun setUserLocationMarker(latlng: LatLng, markerTitle: String, markerSnippet: String) {
        // ?????? marker ??????
        if (currentMarker != null) currentMarker!!.remove()

        // setting marker for current location
        val markerOptions = MarkerOptions()
        markerOptions.position(latlng)
        markerOptions.title(markerTitle)
        markerOptions.snippet(markerSnippet)
        markerOptions.draggable(true)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

        // add the marker to map
        currentMarker = map.addMarker(markerOptions)
    }

    // if app can't get location, use the default location(=seoul)
    private fun setDefaultLocationMarker() {
        // delete existing marker
        currentMarker?.remove()

        // Setting the marker for default location
        val markerOptions = MarkerOptions()
        markerOptions.position(defaultLocation)
        markerOptions.title("???????????? ????????? ??? ??????")
        markerOptions.snippet("?????? ???????????? GPS ?????? ?????? ???????????????")
        markerOptions.draggable(true)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

        // add the marker to map
        currentMarker = map.addMarker(markerOptions)

        // camera update to the default location marker
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM)
        map.moveCamera(cameraUpdate)
    }

    private fun setTodoLocationMarker(id: String, latlng: LatLng, markerTitle: String) {

        // Setting the marker for default location
        val markerOptions = MarkerOptions()
        markerOptions.position(latlng)
        markerOptions.title(markerTitle)
        markerOptions.draggable(true)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

        // add the marker to map
        todoMarkerHashMap[id] = map.addMarker(markerOptions)!!
    }

    // ????????? ????????? ?????? Location ??? ?????? return for maker's title
//    private fun getCurrentAddress(latlng: LatLng): String {
//        // ?????? ????????? ?????????????????? ?????? ???????????? ?????????.
//        val addressList: List<Address>?
//        val geocoder = Geocoder(context, Locale.getDefault())
//        // ??????????????? ???????????? ?????? ???????????? ?????????.
//        addressList = try {
//            geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1)
//        } catch (e: IOException) {
//            Toast.makeText(
//                context,
//                "??????????????? ????????? ????????? ??? ????????????. ??????????????? ???????????? ????????? ????????? ?????????.",
//                Toast.LENGTH_SHORT
//            ).show()
//            e.printStackTrace()
//            return "?????? ?????? ??????"
//        }
//        if (addressList != null) {
//            if (addressList.isEmpty()) { // ?????? ???????????? ??????????????? ?????? ?????????
//                return "?????? ????????? ?????? ??????"
//            }
//        }
//
//        // ????????? ?????? ???????????? ???????????? ??????
//        val address: Address = addressList!![0]
//        val addressStringBuilder = StringBuilder()
//        for (i in 0..address.maxAddressLineIndex) {
//            addressStringBuilder.append(address.getAddressLine(i))
//            if (i < address.maxAddressLineIndex) addressStringBuilder.append("\n")
//        }
//        return addressStringBuilder.toString()
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        map.let {
            outState.putParcelable(KEY_CAMERA_POSITION, it.cameraPosition)
            outState.putParcelable(KEY_LOCATION, currentLatlng)
            super.onSaveInstanceState(outState)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        MapsInitializer.initialize(context)

        map = googleMap
        setDefaultLocationMarker() // location ????????? ?????? ?????? ?????? ???????????? default location ??? ?????? ??????.
        getLocationPermission() // location permission ??????
        updateLocationUI() // activate the user's location
        getUserAccount(currentUser.uid) // generate marker for the user's location in real-time.
        getFriendList() // generate marker for the friends location in real-time.
    }

    // if the location permission is granted, activate the user's location.
    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                map.isMyLocationEnabled = true
                map.uiSettings.isMyLocationButtonEnabled = true
            } else {
                map.isMyLocationEnabled = false
                map.uiSettings.isMyLocationButtonEnabled = false
                currentLatlng = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            e.message?.let { Log.e("Exception: %s", it) }
        }
    }

    // Check location permissions
    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                context,
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) locationPermissionGranted = true
        else ActivityCompat.requestPermissions(
            context,
            arrayOf(ACCESS_FINE_LOCATION),
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) locationPermissionGranted =
                    true
            }
        }
        updateLocationUI()
    }

    override fun onStart() { // ???????????? Fragment ??? ???????????? ?????????.
        super.onStart()
        mapView.onStart()
        Log.d(TAG, "onStart ")
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        Log.d(TAG, "onStop : removeLocationUpdates")
    }

    override fun onResume() { // ???????????? Fragment ??? ????????????, ????????? ??????????????? ???????????? ?????? ??????
        super.onResume()
        mapView.onResume()
        if (locationPermissionGranted) {
            Log.d(TAG, "onResume : requestLocationUpdates")
            if (ActivityCompat.checkSelfPermission(
                    context,
                    ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                shouldShowRequestPermissionRationale("android.permission.ACCESS_FINE_LOCATION")
                permissionLauncher.launch(ACCESS_FINE_LOCATION)
                return
            }
            map.isMyLocationEnabled = true
        }
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroyView() { // ?????????????????? ????????? View ??? ???????????? ??????
        super.onDestroyView()
        Log.d(TAG, "onDestroyView : removeLocationUpdates")
    }

    override fun onDestroy() {
        // Destroy ??? ??????, ????????? OnDestroyView ?????? View ??? ????????????, OnDestroy()??? ????????????.
        super.onDestroy()
        binding.mapView.onDestroy()
    }

}