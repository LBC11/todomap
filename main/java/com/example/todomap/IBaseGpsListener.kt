package com.example.todomap

import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import com.google.firebase.inject.Provider

interface IBaseGpsListener {
    fun onLocationChanged(location: Location)
    fun onProviderDisabled(provider: String)
    fun onProviderEnabled(provider: String)
    fun onStatusChanged(provider: String, status: Int, extras: Bundle?)
    fun onGpsStatusChanged(event: Int)
}