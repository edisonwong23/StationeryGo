package com.example.stationerygo.LoginPage

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentRegisterAddressPageBinding
import com.example.stationerygo.databinding.FragmentRegisterPageBinding
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.async
import java.lang.Exception
import java.util.*

private lateinit var binding: FragmentRegisterAddressPageBinding
private lateinit var fusedLocationClient: FusedLocationProviderClient
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth
private var currentLat: Double ?= null
private var currentLon: Double ?= null
private var address: String ?= null

class RegisterAddressPage : Fragment() {

    var mapView: MapView? = null
    var map: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterAddressPageBinding.inflate(
            inflater,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()

        binding.userAddressEdittextField.isFocusable = false

        val stationeryBind = binding.stateEdittextField
        var stationery = resources.getStringArray(R.array.LivingType)
        stationery = stationery.sortedArray()
        val adapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_1,stationery)
        stationeryBind.setAdapter(adapter)

        mapView = binding.googleMapView
        mapView!!.onCreate(savedInstanceState)

        mapView!!.getMapAsync{
            map = it
            it.uiSettings.isMyLocationButtonEnabled = false

            try {
                MapsInitializer.initialize(requireActivity())
            }catch (e: GooglePlayServicesNotAvailableException){
                Log.d("Maps", "Error: " + e.toString())
            }

            it.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(3.140853,101.693207),12.0f))
        }

        locationSearch()

        binding.userAddressUpdateBtn.setOnClickListener {
            updateAddress()
        }
        // Inflate the layout for this fragment

        return binding.root
    }

    private fun locationSearch(){
        val autoCompleteSearch: AutocompleteSupportFragment = childFragmentManager?.findFragmentById(R.id.usergoogleMapSearchBar) as AutocompleteSupportFragment

        if (!Places.isInitialized()) {
            Places.initialize(context, "AIzaSyCVQbIWF78O7Mt3K7_Os1aqIurOxzSFpVM" )
        }
        val placesClient = Places.createClient(context)

        autoCompleteSearch.setLocationBias(
            RectangularBounds.newInstance(
            LatLng(3.140853,101.693207),LatLng(3.140853,101.693207)
        ))
        autoCompleteSearch.setCountries("MY")

        val token : AutocompleteSessionToken = AutocompleteSessionToken.newInstance()

        autoCompleteSearch.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.ADDRESS))

        autoCompleteSearch.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(status: Status) {
                Log.d("Maps", status.toString())
            }

            override fun onPlaceSelected(place: Place) {
                var address = place.address.toString()
                var LatLng = place.latLng.toString()

                currentLat = place.latLng.latitude
                currentLon = place.latLng.longitude
                address = place.address.toString()

                var placeLat = place.latLng.latitude
                var placeLon = place.latLng.longitude
                var currentAddress = place.address.toString()
                binding.userAddressEdittextField.setText(currentAddress)

                Log.d("Maps", "Current Location: $address \n LatLnh: $LatLng")

                mapView!!.getMapAsync{
                    map = it
                    it.uiSettings.isMyLocationButtonEnabled = false

                    try {
                        MapsInitializer.initialize(requireActivity())
                    }catch (e: GooglePlayServicesNotAvailableException){
                        Log.d("Maps",e.toString())
                    }

                    it.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(placeLat,placeLon),16.0f))

                    it.addMarker(
                        MarkerOptions().position(LatLng(placeLat,placeLon))
                    )
                }
            }

        })

    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    private fun updateAddress(){
        var checkAddress = binding.userAddressTextField?.editText.toString()
        var checkLivingNo = binding.phoneNumberTextfield?.editText.toString()
        var checkLivingType = binding.stateTextField?.editText.toString()
        var newLivingNo = ""
        var newLivingType = ""

        var errorChecker = false

        if(checkAddress.isEmpty()){
            binding.userAddressTextField.error = "Required*"
            errorChecker = true
        }
        else{
            binding.userAddressTextField.isErrorEnabled = false
        }

        if(checkLivingNo.isEmpty()){
            newLivingNo = "None"
        }

        if(checkLivingType.isEmpty()){
            newLivingType = "None"
        }

        if(errorChecker){
            Toast.makeText(context,"Address is Required",Toast.LENGTH_SHORT).show()
        }
        else{
            var uid = auth.currentUser?.uid.toString()
            database = FirebaseDatabase.getInstance().getReference("Users")
            val addressRef = database.child(uid)

            addressRef.child("address").setValue(address).addOnCompleteListener{
                addressRef.child("lat").setValue(currentLat).addOnCompleteListener{
                    addressRef.child("lon").setValue(currentLon).addOnCompleteListener{
                        addressRef.child("livingNo").setValue(newLivingNo).addOnCompleteListener{
                            addressRef.child("livingType").setValue(newLivingType).addOnCompleteListener{
                                findNavController().navigateUp()
                            }
                        }
                    }
                }
            }

        }
    }
}