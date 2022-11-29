package com.example.stationerygo.StoreCreate


import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.stationerygo.LoginPage.imagePathFromFirebase
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentCreateStoreAddressPageBinding
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient

import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.async
import java.lang.Exception
import java.util.*

private lateinit var binding : FragmentCreateStoreAddressPageBinding
private lateinit var fusedLocationClient: FusedLocationProviderClient
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth
private var currentLat : Double ?= null
private var currentLon : Double ?= null
private var currentShopAddress : String ?= null

class CreateStoreAddressPage : Fragment() {

    var mapView: MapView? = null
    var map: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateStoreAddressPageBinding.inflate(
            inflater,
            container,
            false
        )

       binding.shopAddressEdittextField.isFocusable = false

        mapView = binding.shopGoogleMapView
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())


        binding.createStoreBtn.setOnClickListener{
            addressValidation()
        }

        return binding.root
    }



    private fun insertToFileStorage(){
        val data = arguments?.getString("storeImage")?.toUri()
        Log.d("Store",data.toString())
        if(data != null){
            val progress = ProgressDialog(activity)
            progress.setTitle("Uploading Image To Storage")
            progress.show()
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val refStorage = FirebaseStorage.getInstance().reference.child("store_images/$fileName")
            refStorage.putFile(data).addOnSuccessListener(
                OnSuccessListener<UploadTask.TaskSnapshot>{
                        taskSnapshot -> taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                    imagePathFromFirebase = it.toString()
                    progress.dismiss()
                    getCurrentUser(imagePathFromFirebase)
                }
                }).addOnFailureListener{
                    Log.d("Store",it.toString())
            }
        }
    }

    private fun getCurrentUser(imagePathFromFirebase:String){
        auth = Firebase.auth
        val currentUser = Firebase.auth.currentUser
        currentUser?.let {
            var getCurrentUser = currentUser.email.toString()
            var uid = currentUser.uid.toString()
            insetToDatabase(imagePathFromFirebase,getCurrentUser,uid)
        }
    }

    private fun insetToDatabase(imagePathFromFirebase:String,currentUser:String,uid:String){


        val progress = ProgressDialog(activity)
        progress.setTitle("Uploading Store Details")
        progress.show()

        database = FirebaseDatabase.getInstance().getReference("Stores")
        val storeID = UUID.randomUUID().toString()
        val storeName = arguments?.getString("storeName").toString()
        val description = arguments?.getString("description").toString()
        val timeStart = arguments?.getString("timeStart").toString()
        val timeEnd = arguments?.getString("timeEnd").toString()
        val operatingDay = arguments?.getString("operatingDays").toString()
//        val operatingDay = "None"
        val email = arguments?.getString("email").toString()
        val phone = arguments?.getString("phone").toString()

        val user = CreateStoreData(
            storeID,
            storeName,
            description,
            timeStart,
            timeEnd,
            operatingDay,
            email,
            phone,
            currentShopAddress,
            currentLat.toString(),
            currentLon.toString()
            ,imagePathFromFirebase)

        database.child(uid).setValue(user)
            .addOnCompleteListener {
            progress.hide()
                findNavController().navigate(R.id.action_createStoreAddressPage_to_mainStorePage)
//            findNavController().navigateUp()

            }.addOnFailureListener{
            Log.d("Store", "Failure happen" +it.toString())
            Toast.makeText(getContext(), it.toString(), Toast.LENGTH_SHORT).show()
                progress.hide()
        }
    }

    private fun addressValidation(){
        var address = binding.shopAddressTextField.editText?.text.toString()
        var errorChecker = false

        if(address.isEmpty()){
            binding.shopAddressTextField.error = "Required*"
            errorChecker = true
        }
        else{
            binding.shopAddressTextField.isErrorEnabled = false
        }

        if(errorChecker){
            Toast.makeText(context,"Check Input Boxes",Toast.LENGTH_SHORT).show()
        }
        else{
            insertToFileStorage()
        }
    }
    private fun locationSearch(){
        val autoCompleteSearch: AutocompleteSupportFragment = childFragmentManager?.findFragmentById(R.id.shopGoogleMapSearchBar) as AutocompleteSupportFragment

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

                var LatLng = place.latLng.toString()
                var placeLat = place.latLng.latitude
                var placeLon = place.latLng.longitude
                var currentAddress = place.address.toString()

                currentLat = placeLat
                currentLon = placeLon
                currentShopAddress = currentAddress

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