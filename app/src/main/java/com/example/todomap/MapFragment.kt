package com.example.todomap

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.example.todomap.calendar.TodoViewModel
import com.example.todomap.databinding.FragmentMapBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap


class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        private const val TAG = "MapFragment"
        private const val DEFAULT_ZOOM = 15F
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val GPS_ENABLE_REQUEST_CODE = 2001
        private const val UPDATE_INTERVAL_MS = 1000 * 60 * 15 // 1분 단위 시간 갱신
        private const val FASTEST_UPDATE_INTERVAL_MS = 1000 * 30 // 30초 단위로 화면 갱신
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

    // Entry point to fused location provider
//    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
//    private lateinit var locationRequest: LocationRequest

    private var currentMarker: Marker? = null
    private var currentLocation: Location? = null
    private var cameraPosition: Location? = null

    // defaultLocation: Seoul
    private val defaultLocation = LatLng(37.56, 126.97)

    private var locationPermissionGranted = false

    private var friendsUid: MutableList<String> = arrayListOf()
    private var locationListenerHashMap = HashMap<String, ValueEventListener>()
    private var accountListenerHashMap = HashMap<String, ValueEventListener>()


//    private val markerHashMap = HashMap<String, Marker>()

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var userLocationRef: DatabaseReference
    private lateinit var friendRef: DatabaseReference
    private lateinit var friendsLocationRef: DatabaseReference
    private lateinit var friendsAccountRef: DatabaseReference

    override fun onAttach(activity: Activity) { // Fragment 가 Activity 에 attach 될 때 호출된다.
        context = activity as FragmentActivity
        super.onAttach(activity)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        savedInstanceState?.let {
            currentLocation = it.getParcelable(KEY_LOCATION)
            cameraPosition = it.getParcelable(KEY_CAMERA_POSITION)
        }
        binding = FragmentMapBinding.inflate(inflater, container, false)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)

        binding.mapView.getMapAsync(this)

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid.toString()
        val email = currentUser?.email.toString()

        database = FirebaseDatabase.getInstance().reference
        userLocationRef = database.child("location").child(uid)
        friendRef = database.child("friend").child(uid)
        friendsLocationRef = database.child("location")
        friendsAccountRef = database.child("userAccount")

        return binding.root
    }

    private fun addMarker(){
        currentMarker?.remove()

        // Setting the marker for default location
        val markerOptions = MarkerOptions()
        markerOptions.position(defaultLocation)
        markerOptions.title("위치정보 가져올 수 없음")
        markerOptions.snippet("위치 퍼미션과 GPS 활성 여부 확인하세요")
        markerOptions.draggable(true)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

        // add the marker to map
//        markerHashMap["a"] = map.addMarker(markerOptions)!!
        map.addMarker(markerOptions)!!
    }

    private fun getFriendList() {
        friendRef.addValueEventListener(friendListListener())
    }

    private fun friendListListener() = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                val hash = snapshot.value as HashMap<*, *>?
                if (friendsUid.isNotEmpty()) {
                    friendsUid.forEach {
                        // remove the existing listener
                        friendsAccountRef.child(it)
                            .removeEventListener(accountListenerHashMap[it]!!)
                        friendsLocationRef.child(it)
                            .removeEventListener(locationListenerHashMap[it]!!)

                        // clear the hash map
                        accountListenerHashMap.clear()
                        locationListenerHashMap.clear()
                    }

                    friendsUid.clear()
                }

                hash?.forEach {
                    friendsUid.add(it.value.toString())
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
        if(friendsUid.isNotEmpty()) {
            friendsUid.forEach {
                val listener = friendAccountListener(it)
                accountListenerHashMap[it] = listener
                friendsAccountRef.child(it).addValueEventListener(listener)
            }
        }
    }

    private fun getFriendLocation(uid: String, userName: String, info: String) {
        val listener = friendLocationListener(userName, info)
        locationListenerHashMap[uid] = listener
        friendsLocationRef.child(uid).addValueEventListener(listener)

    }

    private fun friendAccountListener(uid: String) = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                Log.d(TAG, "success to load friend's account in DB")

                if(locationListenerHashMap[uid] != null) {
                    friendsLocationRef.child(uid).removeEventListener(locationListenerHashMap[uid]!!)
                }

                val hash = snapshot.value as HashMap<*, *>?
                getFriendLocation(uid, hash?.get("userName").toString(), hash?.get("info").toString())

            } else {
                Log.d(TAG, "There are no friend's location in DB")
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.d(TAG, "Failed to load friend's location in DB")
        }
    }

    private fun friendLocationListener(userName: String, info: String) = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                Log.d(TAG, "success to load friend's location in DB")

                val hash = snapshot.value as HashMap<*, *>?
                val latlng = LatLng(
                    hash?.get("latitude") as Double,
                    hash["longitude"] as Double
                )

                setCurrentUserLocation(latlng, userName, info)

            } else {
                Log.d(TAG, "There are no friend's location in DB")
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.d(TAG, "Failed to load friend's location in DB")
        }
    }

    private fun getUserLocation() {
        userLocationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "success to load user's location in DB")
                    val hash = snapshot.value as HashMap<*, *>?
                    val latlng = LatLng(hash?.get("latitude") as Double,
                        hash["longitude"] as Double
                    )

                    setCurrentUserLocation(latlng, "user", "current location")

                } else {
                    Log.d(TAG, "There are no user's location in DB")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "Failed to load friend list in DB")
            }
        })
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        // Fragment 에서의 OnCreateView 를 마치고, Activity 에서 onCreate()가 호출되고 나서 호출되는 메소드이다.
        // Activity 와 Fragment 의 뷰가 모두 생성된 상태로, View 를 변경하는 작업이 가능한 단계다.
        super.onActivityCreated(savedInstanceState)
        //액티비티가 처음 생성될 때 실행되는 함수
        MapsInitializer.initialize(context)
//        locationRequest = LocationRequest()
//            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // 정확도를 최우선적으로 고려
//            .setInterval(UPDATE_INTERVAL_MS.toLong()) // 위치가 Update 되는 주기
//            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS.toLong()) // 위치 획득후 업데이트되는 주기
//        val builder = LocationSettingsRequest.Builder()
//        builder.addLocationRequest(locationRequest)

        // FusedLocationProviderClient 객체 생성
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        map.let{
            outState.putParcelable(KEY_CAMERA_POSITION, it.cameraPosition)
            outState.putParcelable(KEY_LOCATION, currentLocation)
            super.onSaveInstanceState(outState)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setDefaultLocation() // location 정보를 얻지 못할 때를 대비해서 default location 에 따라 설정.
        getLocationPermission() // location permission 요청
        updateLocationUI() // activate the user's location
        getUserLocation() // generate marker for the user's location in real time.
        getFriendList() // get uid of friends

//        getDeviceLocation() // user's location 에 따라 현재 위치 정보 설정
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
                currentLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            e.message?.let { Log.e("Exception: %s", it) }
        }
    }

    // if app can't get location, use the default location(=seoul)
    private fun setDefaultLocation() {
        // delete existing marker
        currentMarker?.remove()

        // Setting the marker for default location
        val markerOptions = MarkerOptions()
        markerOptions.position(defaultLocation)
        markerOptions.title("위치정보 가져올 수 없음")
        markerOptions.snippet("위치 퍼미션과 GPS 활성 여부 확인하세요")
        markerOptions.draggable(true)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

        // add the marker to map
        currentMarker = map.addMarker(markerOptions)

        // camera update to the default location marker
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM)
        map.moveCamera(cameraUpdate)
    }

    fun setCurrentUserLocation(latlng: LatLng, markerTitle: String?, markerSnippet: String?) {

        // 기존 marker 삭제
        if (currentMarker != null) currentMarker!!.remove()

        // setting marker for current location
        val markerOptions = MarkerOptions()
        markerOptions.position(latlng)
        markerOptions.title(markerTitle)
        markerOptions.snippet(markerSnippet)
        markerOptions.draggable(true)

        // add the marker to map
        currentMarker = map.addMarker(markerOptions)

        // camera update to the default location marker
        val cameraUpdate = CameraUpdateFactory.newLatLng(latlng)
        map.moveCamera(cameraUpdate)
    }

    // 위도와 경도를 읽고 Location 의 주소 return for maker's title
    fun getCurrentAddress(latlng: LatLng): String {
        // 위치 정보와 지역으로부터 주소 문자열을 구한다.
        var addressList: List<Address>?
        val geocoder = Geocoder(context, Locale.getDefault())
        // 지오코더를 이용하여 주소 리스트를 구한다.
        addressList = try {
            geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1)
        } catch (e: IOException) {
            Toast.makeText(context, "위치로부터 주소를 인식할 수 없습니다. 네트워크가 연결되어 있는지 확인해 주세요.", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
            return "주소 인식 불가"
        }
        if (addressList != null) {
            if (addressList.isEmpty()) { // 주소 리스트가 비어있는지 비어 있으면
                return "해당 위치에 주소 없음"
            }
        }

        // 주소를 담는 문자열을 생성하고 리턴
        val address: Address = addressList!![0]
        val addressStringBuilder = StringBuilder()
        for (i in 0..address.maxAddressLineIndex) {
            addressStringBuilder.append(address.getAddressLine(i))
            if (i < address.maxAddressLineIndex) addressStringBuilder.append("\n")
        }
        return addressStringBuilder.toString()
    }

    // location request 에 성공하면 진행되는 프로세스
//    private var locationCallback: LocationCallback = object : LocationCallback() {
//        override fun onLocationResult(locationResult: LocationResult) {
//            super.onLocationResult(locationResult)
//
//            // result 를 list 로 받음
//            val locationList = locationResult.locations
//
//            // list 가 비어있지 않다면 location 설정
//            if (locationList.size > 0) {
//
//                // 가장 최신 location 사용
//                val location = locationList[locationList.size - 1]
//                val currentPosition = LatLng(location.latitude, location.longitude)
//                val markerTitle = getCurrentAddress(currentPosition)
//                val markerSnippet = "위도:" + location.latitude.toString() + " 경도:" + location.longitude.toString()
//
//                //현재 위치에 마커 생성하고 이동
//                setCurrentLocation(location, markerTitle, markerSnippet)
//                currentLocation = location
//            }
//        }
//    }

//    private fun getDeviceLocation() {
//        try {
//            if (locationPermissionGranted) fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
//        } catch (e: SecurityException) {
//            Log.e("Exception: %s", e.message!!)
//        }
//    }

    // Check location permissions
    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) locationPermissionGranted = true
        else ActivityCompat.requestPermissions(context, arrayOf(ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) { PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) locationPermissionGranted = true
            }
        }
        updateLocationUI()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkLocationServicesStatus(): Boolean {
        val locationManager = requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onStart() { // 유저에게 Fragment 가 보이도록 해준다.
        super.onStart()
        mapView.onStart()
        Log.d(TAG, "onStart ")
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        Log.d(TAG, "onStop : removeLocationUpdates")
//        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() { // 유저에게 Fragment 가 보여지고, 유저와 상호작용이 가능하게 되는 부분
        super.onResume()
        mapView.onResume()
        if (locationPermissionGranted) {
            Log.d(TAG, "onResume : requestLocationUpdates")
            if (ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                shouldShowRequestPermissionRationale("android.permission.ACCESS_FINE_LOCATION")
                permissionLauncher.launch(ACCESS_FINE_LOCATION)
                return
            }
//            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
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

    override fun onDestroyView() { // 프래그먼트와 관련된 View 가 제거되는 단계
        super.onDestroyView()
        Log.d(TAG, "onDestroyView : removeLocationUpdates")
//        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        // Destroy 할 때는, 반대로 OnDestroyView 에서 View 를 제거하고, OnDestroy()를 호출한다.
        super.onDestroy()
        binding.mapView.onDestroy()
    }

}