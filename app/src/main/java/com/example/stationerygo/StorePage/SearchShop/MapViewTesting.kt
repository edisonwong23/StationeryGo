package com.example.stationerygo.StorePage.SearchShop

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentMapViewTestingBinding
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.util.*


private lateinit var binding: FragmentMapViewTestingBinding

class MapViewTesting : Fragment() {

    var mapView: MapView? = null
    var map: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapViewTestingBinding.inflate(
            inflater,
            container,
            false
        )

        locationSearch()

        mapView = binding.googleMapView
        mapView!!.onCreate(savedInstanceState)

        mapView!!.getMapAsync{
            map = it
            it.uiSettings.isMyLocationButtonEnabled = false

            try {
                MapsInitializer.initialize(requireActivity())
            }catch (e: GooglePlayServicesNotAvailableException){
                Log.d("Maps",e.toString())
            }

            it.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(3.140853,101.693207),12.0f))
        }


        return binding.root
    }

    private fun locationSearch(){
//        val autoCompleteSearch : AutocompleteSupportFragment = activity?.supportFragmentManager?.findFragmentById(R.id.googleMapSearchBar) as? AutocompleteSupportFragment
          val autoCompleteSearch: AutocompleteSupportFragment = childFragmentManager?.findFragmentById(R.id.googleMapSearchBar) as AutocompleteSupportFragment

        if (!Places.isInitialized()) {
            Places.initialize(context, "AIzaSyCVQbIWF78O7Mt3K7_Os1aqIurOxzSFpVM" )
        }
        val placesClient = Places.createClient(context)

        autoCompleteSearch.setLocationBias(RectangularBounds.newInstance(
            LatLng(3.140853,101.693207),LatLng(3.140853,101.693207)
        ))
        autoCompleteSearch.setCountries("MY")

        val token : AutocompleteSessionToken = AutocompleteSessionToken.newInstance()

        autoCompleteSearch.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.ADDRESS))

        autoCompleteSearch.setOnPlaceSelectedListener(object : PlaceSelectionListener{
            override fun onError(status: Status) {
                Log.d("Maps", status.toString())
            }

            override fun onPlaceSelected(place: Place) {
                var address = place.address.toString()
                var LatLng = place.latLng.toString()

                var currentLat = place.latLng.latitude
                var currentLng = place.latLng.longitude
                Log.d("Maps", "Current Location: $address \n LatLnh: $LatLng")

                mapView!!.getMapAsync{
                    map = it
                    it.uiSettings.isMyLocationButtonEnabled = false

                    try {
                        MapsInitializer.initialize(requireActivity())
                    }catch (e: GooglePlayServicesNotAvailableException){
                        Log.d("Maps",e.toString())
                    }

                    it.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLat,currentLng),16.0f))
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

