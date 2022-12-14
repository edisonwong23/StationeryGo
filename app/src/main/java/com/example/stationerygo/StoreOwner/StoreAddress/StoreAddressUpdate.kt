package com.example.stationerygo.StoreOwner.StoreAddress

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentRegisterAddressPageBinding
import com.example.stationerygo.databinding.FragmentStoreAddressUpdateBinding
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.util.*

private lateinit var binding: FragmentStoreAddressUpdateBinding
private lateinit var auth: FirebaseAuth
private lateinit var database : DatabaseReference
private var updateLat: Double ?= null
private var updateLon: Double ?= null
private var updateaddress: String ?= null

class StoreAddressUpdate : Fragment() {

    var mapView: MapView? = null
    var map: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding = FragmentStoreAddressUpdateBinding.inflate(
            inflater,
            container,
            false
        )
        auth = FirebaseAuth.getInstance()

        getShopAddress()

        locationSearch()



        mapView = binding.shopGoogleMapViewUpdate
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

        binding.createStoreBtn.setOnClickListener {
            val alartDialog = AlertDialog.Builder(context,R.style.AlertDialogCustom)
            alartDialog.apply {
                setTitle("Update Store Location?")
                setMessage("Confirm New Store Location?")
                setPositiveButton("Confirm"){ _: DialogInterface?, _: Int ->
                    updateStoreLocation()
                }
                setNegativeButton("Cancel"){_, _ ->
                }
            }.create().show()

        }

        return binding.root
    }

    private fun updateStoreLocation(){
        var uid = auth.currentUser?.uid.toString()
        database = FirebaseDatabase.getInstance().getReference("Stores")
        val updateRef = database.child(uid)

        updateRef.child("address").setValue(updateaddress).addOnCompleteListener{
            updateRef.child("lat").setValue(updateLat.toString()).addOnCompleteListener{
                updateRef.child("lon").setValue(updateLon.toString()).addOnCompleteListener {
                    Toast.makeText(context,"Location Updated!",Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun getShopAddress(){
        var uid = auth.currentUser?.uid.toString()
        database = FirebaseDatabase.getInstance().getReference("Stores")
        var dataRef = database.child(uid)

        val postListener  = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var address = snapshot.child("address").value.toString()
                var lat = snapshot.child("lat").value.toString()
                var lon = snapshot.child("lon").value.toString()

                updateLat = lat.toDouble()
                updateLon = lon.toDouble()
                updateaddress = address

                binding.shopAddressEdittextField.setText(address)

                mapView!!.getMapAsync{
                    map = it
                    it.uiSettings.isMyLocationButtonEnabled = false

                    try {
                        MapsInitializer.initialize(requireActivity())
                    }catch (e: GooglePlayServicesNotAvailableException){
                        Log.d("Maps",e.toString())
                    }

                    it.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat.toDouble(),lon.toDouble()),16.0f))

                    it.addMarker(
                        MarkerOptions().position(LatLng(lat.toDouble(),lon.toDouble()))
                    )
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        dataRef.addListenerForSingleValueEvent(postListener)
    }

    private fun locationSearch(){
        val autoCompleteSearch: AutocompleteSupportFragment = childFragmentManager?.findFragmentById(R.id.shopSearchGoogleMapUpdate) as AutocompleteSupportFragment

        if (!Places.isInitialized()) {
            Places.initialize(context, "AIzaSyCVQbIWF78O7Mt3K7_Os1aqIurOxzSFpVM" )
        }
        val placesClient = Places.createClient(context)

        autoCompleteSearch.setLocationBias(
            RectangularBounds.newInstance(
                LatLng(3.140853,101.693207), LatLng(3.140853,101.693207)
            ))
        autoCompleteSearch.setCountries("MY")

        val token : AutocompleteSessionToken = AutocompleteSessionToken.newInstance()

        autoCompleteSearch.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.ADDRESS))

        autoCompleteSearch.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(status: Status) {
                Log.d("Maps", status.toString())
            }

            override fun onPlaceSelected(place: Place) {

                var LatLng = place.latLng.toString()


                var placeLat = place.latLng.latitude
                var placeLon = place.latLng.longitude
                var currentAddress = place.address.toString()

                updateLat = placeLat
                updateLon = placeLon
                updateaddress = currentAddress

                binding.shopAddressEdittextField.setText(currentAddress)

                Log.d("Maps", "Current Location: $currentAddress \n LatLnh: $LatLng")

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
}