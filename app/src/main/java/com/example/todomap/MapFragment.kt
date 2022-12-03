package com.example.todomap

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.R
import android.app.Activity
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
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
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var context: FragmentActivity
    private lateinit var binding: FragmentMapBinding
    private val todoViewModel: TodoViewModel by viewModels()

    // GoogleMap variables
    private val TAG = "ITM"
    private lateinit var map: GoogleMap
    private lateinit var mapView: MapView
    private var currentMarker: Marker? = null

    // Permission Launcher
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    // Entry point to fused location provider
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var currentLocation: Location? = null

    // defaultLocation: Seoul
    private val defaultLocation = LatLng(37.56, 126.97)

    private val DEFAULT_ZOOM = 15
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var locationPermissionGranted = false

    private val GPS_ENABLE_REQUEST_CODE = 2001
    private val UPDATE_INTERVAL_MS = 1000 * 60 * 1 // 1분 단위 시간 갱신
    private val FASTEST_UPDATE_INTERVAL_MS = 1000 * 30 // 30초 단위로 화면 갱신

    private val KEY_CAMERA_POSITION = "camera_position"
    private val KEY_LOCATION = "location"


    override fun onAttach(activity: Activity) { // Fragment 가 Activity 에 attach 될 때 호출된다.
        context = activity as FragmentActivity
        super.onAttach(activity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 초기화 해야 하는 리소스들을 여기서 초기화 해준다.
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (savedInstanceState != null) {
            currentLocation = savedInstanceState.getParcelable(KEY_LOCATION)!!
            val cameraPosition: CameraPosition? = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }
        binding = FragmentMapBinding.inflate(inflater, container, false)

        mapView = binding.mapView
        if (mapView != null) {
            mapView!!.onCreate(savedInstanceState)
        }

        binding.mapView.getMapAsync(this)

        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        // Fragment 에서의 OnCreateView 를 마치고, Activity 에서 onCreate()가 호출되고 나서 호출되는 메소드이다.
        // Activity 와 Fragment 의 뷰가 모두 생성된 상태로, View 를 변경하는 작업이 가능한 단계다.
        super.onActivityCreated(savedInstanceState)
        //액티비티가 처음 생성될 때 실행되는 함수
        MapsInitializer.initialize(context)
        locationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY) // 정확도를 최우선적으로 고려
            .setInterval(UPDATE_INTERVAL_MS.toLong()) // 위치가 Update 되는 주기
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS.toLong()) // 위치 획득후 업데이트되는 주기
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)

        // FusedLocationProviderClient 객체 생성
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setDefaultLocation() // GPS 를 찾지 못하는 장소에 있을 경우 지도의 초기 위치가 필요함.
        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()
    }

    private fun addMarker(){

    }

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

    private fun setDefaultLocation() {
        currentMarker?.remove()
        val markerOptions = MarkerOptions()
        markerOptions.position(defaultLocation)
        markerOptions.title("위치정보 가져올 수 없음")
        markerOptions.snippet("위치 퍼미션과 GPS 활성 여부 확인하세요")
        markerOptions.draggable(true)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        currentMarker = map.addMarker(markerOptions)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f)
        map.moveCamera(cameraUpdate)
    }

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

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                val location = locationList[locationList.size - 1]
                val currentPosition = LatLng(location.latitude, location.longitude)
                val markerTitle = getCurrentAddress(currentPosition)
                val markerSnippet =
                    "위도:" + location.latitude.toString() + " 경도:" + location.longitude.toString()
                Log.d(TAG, "Time :" + CurrentTime().toString() + " onLocationResult : " + markerSnippet)

                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet)
                currentLocation = location
            }
        }
    }

    private fun CurrentTime(): String? {
        val today = Date()
        val date = SimpleDateFormat("yyyy/MM/dd", Locale.KOREA)
        val time = SimpleDateFormat("hh:mm:ss a", Locale.KOREA)
        return time.format(today)
    }

    fun setCurrentLocation(location: Location, markerTitle: String?, markerSnippet: String?) {
        if (currentMarker != null) currentMarker!!.remove()
        val currentLatLng = LatLng(location.latitude, location.longitude)
        val markerOptions = MarkerOptions()
        markerOptions.position(currentLatLng)
        markerOptions.title(markerTitle)
        markerOptions.snippet(markerSnippet)
        markerOptions.draggable(true)
        currentMarker = map.addMarker(markerOptions)
        val cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng)
        map.moveCamera(cameraUpdate)
    }

    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message!!)
        }
    }

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
        mapView!!.onStart()
        Log.d(TAG, "onStart ")
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
        Log.d(TAG, "onStop : removeLocationUpdates")
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() { // 유저에게 Fragment 가 보여지고, 유저와 상호작용이 가능하게 되는 부분
        super.onResume()
        mapView!!.onResume()
        if (locationPermissionGranted) {
            Log.d(TAG, "onResume : requestLocationUpdates")
            if (ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                shouldShowRequestPermissionRationale("android.permission.ACCESS_FINE_LOCATION")
                permissionLauncher.launch(ACCESS_FINE_LOCATION)
                return
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
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
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        // Destroy 할 때는, 반대로 OnDestroyView 에서 View 를 제거하고, OnDestroy()를 호출한다.
        super.onDestroy()
        binding.mapView.onDestroy()
    }

}