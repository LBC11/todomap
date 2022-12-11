package com.example.todomap.calendar

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.todomap.R
import com.example.todomap.databinding.FragmentAutocompleteBinding
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

class AutocompleteFragment : AutocompleteSupportFragment() {

    private val TAG = "AutocompleteFragment"
    private lateinit var binding : FragmentAutocompleteBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAutocompleteBinding.inflate(inflater, container, false)

        setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))
        setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: ${place.name}, ${place.id}")
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        })
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_autocomplete, container, false)
    }
}