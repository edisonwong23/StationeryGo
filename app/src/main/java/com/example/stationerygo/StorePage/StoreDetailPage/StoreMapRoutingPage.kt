package com.example.stationerygo.StorePage.StoreDetailPage

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentStoreDetailPageBinding
import com.example.stationerygo.databinding.FragmentStoreMapRoutingPageBinding
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

private lateinit var binding: FragmentStoreMapRoutingPageBinding
private lateinit var auth: FirebaseAuth
private lateinit var database: DatabaseReference

class StoreMapRoutingPage : Fragment() {

    var mapView: MapView? = null
    var map: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStoreMapRoutingPageBinding.inflate(
            inflater,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()
        mapView = binding.userRoutingMapView
        mapView!!.onCreate(savedInstanceState)

        loadUserLatLon()

//        action_storeDetailPage_to_storeMapRoutingPage2

        return binding.root
    }

    private fun loadUserLatLon(){
        var uid = auth.currentUser?.uid.toString()

        database = FirebaseDatabase.getInstance().getReference("Users")
        var userRef = database.child(uid)

        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var lat = snapshot.child("lat").value.toString()
                var lon = snapshot.child("lon").value.toString()

                loadStoreLatLon(lat,lon)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
         userRef.addListenerForSingleValueEvent(postListener)
    }

    private fun loadStoreLatLon(userLat:String,userLon:String){
        var storeID = arguments?.getString("storeID").toString()
//        Log.d("Routing","$storeID")
        database = FirebaseDatabase.getInstance().getReference("Stores")

        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val loadStoreID = it.child("storeID").value.toString()
                    if(loadStoreID == storeID){
                        val storeLat = it.child("lat").value.toString()
                        val storeLon = it.child("lon").value.toString()

//                        Log.d("Routing","$storeLat , $storeLon")
                        mapRouting(userLat,userLon,storeLat,storeLon)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        database.addListenerForSingleValueEvent(postListener)
    }

    private fun mapRouting(userLat: String, userLon: String, storeLat: String, storeLon: String){

        mapView!!.getMapAsync{
            map = it
            it.uiSettings.isMyLocationButtonEnabled = false

            try {
                MapsInitializer.initialize(requireActivity())
            }catch (e: GooglePlayServicesNotAvailableException){
                Log.d("Routing",e.toString())
            }

            val userLanLon = LatLng(userLat.toDouble(),userLon.toDouble())
            val storeLanLon = LatLng(storeLat.toDouble(),storeLon.toDouble())

            it.addPolyline(PolylineOptions().clickable(true).add(
                userLanLon,
                storeLanLon
            ))

            it.addMarker(MarkerOptions().position(userLanLon).title("You"))
            it.addMarker(MarkerOptions().position(storeLanLon).title("Store"))

            it.moveCamera(CameraUpdateFactory.newLatLngZoom(userLanLon, 16f))

        }
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