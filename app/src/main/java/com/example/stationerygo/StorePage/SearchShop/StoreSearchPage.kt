package com.example.stationerygo.StorePage.SearchShop

import android.app.ProgressDialog
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stationerygo.R
import com.example.stationerygo.StorePage.StoreListAdapter
import com.example.stationerygo.StorePage.StoreListData
import com.example.stationerygo.databinding.FragmentStoreSearchPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private lateinit var binding: FragmentStoreSearchPageBinding
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth

class StoreSearchPage : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStoreSearchPageBinding.inflate(
            inflater,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()

        getUserLatLon()

        return binding.root
    }

    private fun searchedStore(userLat:Double,userLon:Double){
        val progress = ProgressDialog(activity)
        progress.setTitle("Searching Stores, Please wait")
        progress.show()

        var storeArrayList = ArrayList<StoreListData>()
        var searchedStore = arguments?.getString("storeSearch").toString()

        database = FirebaseDatabase.getInstance().getReference("Stores")

        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                storeArrayList.clear()
                var i = 0

                snapshot.children.forEach {

                    var compareName = it.child("storeName").value.toString()
                    if(compareName.contains(searchedStore)){

                        var position = i++
                        var storeImage = it.child("storeImage").value.toString()
                        var storeName = it.child("storeName").value.toString()
                        var startTime = it.child("startTime").value.toString()
                        var endTime = it.child("endTime").value.toString()
                        var operatingDays = it.child("operatingDay").value.toString()
                        var storeLat = it.child("lat").value.toString().toDouble()
                        var storeLon = it.child("lon").value.toString().toDouble()

                        val sdf = SimpleDateFormat("HH:mm")
                        val currentDate = sdf.format(Date())
                        val checkCurrentTime = SimpleDateFormat("HH:mm").parse(currentDate)
                        val checkOpeningTime = SimpleDateFormat("HH:mm").parse(startTime)
                        val checkEndingTime = SimpleDateFormat("HH:mm").parse(endTime)
                        var storeStatus = ""
                        var distance : Float ?= null
                        val result = FloatArray(5)

                        Location.distanceBetween(userLat,userLon,storeLat,storeLon,result)
                        Log.d("Location",result[0].toString())
                        distance = result[0] / 1000

                        val dateFormat = SimpleDateFormat("dd-MM=yyyy")
                        val currentDay = dateFormat.format(Date())
                        val checkCurretDay = SimpleDateFormat("dd-MM=yyyy").parse(currentDay)

                        val cal = Calendar.getInstance()
                        cal.time = checkCurretDay

                        var shopOpenDay = false
                        if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
                            if(operatingDays.contains("Sunday"))
                                shopOpenDay = true
                        }
                        else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY){
                            if(operatingDays.contains("Monday"))
                                shopOpenDay = true
                        }
                        else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY){
                            if(operatingDays.contains("Tuesday"))
                                shopOpenDay = true
                        }
                        else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY){
                            if(operatingDays.contains("Wednesday"))
                                shopOpenDay = true
                        }
                        else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY){
                            if(operatingDays.contains("Thursday"))
                                shopOpenDay = true
                        }
                        else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY){
                            if(operatingDays.contains("Friday"))
                                shopOpenDay = true
                        }
                        else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
                            if(operatingDays.contains("Saturday"))
                                shopOpenDay = true
                        }
                        else{
                            Log.d("Shop","Currently Close")
                        }

                        if(shopOpenDay == true){
                            if(checkCurrentTime.after(checkOpeningTime) && checkCurrentTime.before(checkEndingTime)){
                                storeStatus = "Open"
                            }
                            else
                                storeStatus = "Close"
                        }
                        else
                            storeStatus = "Close"


                        storeArrayList.add(StoreListData(
                            position,
                            storeImage,
                            storeName,
                            "%.2f KM".format(distance),
                            startTime,
                            endTime,
                            storeStatus))
                    }

                    if(!storeArrayList.isEmpty()){
//                        Log.d("Search","Here")
                        binding.noStoreFoundImg.visibility = View.GONE
                        binding.noStoreFoundTxt.visibility = View.GONE
                        binding.recyclerviewStores.visibility = View.VISIBLE
                    }
                    else{
                        binding.noStoreFoundImg.visibility = View.VISIBLE
                        binding.noStoreFoundTxt.visibility = View.VISIBLE
                    }

                    var sortedStoreList = storeArrayList.sortedBy { it.city?.replace("KM","")?.toDouble() }

                    val recyclerView = binding.recyclerviewStores
                    recyclerView.layoutManager = LinearLayoutManager(context)

                    recyclerView.adapter = StoreListAdapter(sortedStoreList){ StoreListData,position ->
                        var bundle = bundleOf(
                            "storename" to sortedStoreList[position].storeName,
                            "storeStatus" to sortedStoreList[position].status,
                        )
                        findNavController().navigate(R.id.action_storeSearchPage_to_storeDetailPage,bundle)
                    }
                    progress.dismiss()
                    }
                }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,"Error Loading From Database",Toast.LENGTH_SHORT).show()
            }

        }

        database.addValueEventListener(postListener)
    }

    private fun getUserLatLon(){
        var uid = auth.currentUser?.uid.toString()
        database = FirebaseDatabase.getInstance().getReference("Users")
        var getUserRef = database.child(uid)

        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var lat = snapshot.child("lat").value.toString()
                var lon = snapshot.child("lon").value.toString()

                searchedStore(lat.toDouble(),lon.toDouble())
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        getUserRef.addListenerForSingleValueEvent(postListener)
    }
}