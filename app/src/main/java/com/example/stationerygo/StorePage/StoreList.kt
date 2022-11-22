package com.example.stationerygo.StorePage

import android.app.ProgressDialog
import android.icu.util.LocaleData
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
import com.example.stationerygo.databinding.FragmentStoreListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

private lateinit var binding: FragmentStoreListBinding
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth
private  var storeID = ""
private var totalItem : String ?= null

class StoreList : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStoreListBinding.inflate(
            inflater,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()

        getUserLatLon()



        binding.goToSearchBtn.setOnClickListener {
//            findNavController().navigate(R.id.action_homePage_to_mapViewTesting)
            findNavController().navigate(R.id.action_homePage_to_registerAddressPage)
//              findNavController().navigate(R.id.action_homePage_to_userAddressPage)
        }

        binding.navigateToCartFAB.setOnClickListener{
            var bundle = bundleOf(
                "StoreID" to storeID,
            )
//            Log.d("Payment","ID: $dataID - Name: $dataName")
            findNavController().navigate(R.id.action_homePage_to_cart_Page,bundle)
        }

        binding.searchStoreButton.setOnClickListener {

            var storeSearch = binding.searchStoreTextInputLayout.editText?.text.toString()

            if(storeSearch.isEmpty()){
                binding.searchStoreTextInputLayout.error = "Cannot be Empty*"
            }
            else{
                var bundle = bundleOf(
                    "storeSearch" to storeSearch,
                )
                findNavController().navigate(R.id.action_homePage_to_storeSearchPage,bundle)
            }


        }

        return binding.root
    }

    private fun checkCartExist(){
        var uid = auth.currentUser?.uid.toString()
        database = FirebaseDatabase.getInstance().getReference("Cart")
        var cartRef = database.child(uid)

        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    snapshot.children.forEach {
                        storeID = it.key.toString()
                        var total =  it.child(storeID).childrenCount
                    }
                    checkItemCount()
                    Log.d("main", storeID)
                    binding.navigateToCartFrame.visibility = View.VISIBLE

                }
                else
                    binding.navigateToCartFrame.visibility = View.GONE

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        }
        cartRef.addListenerForSingleValueEvent(postListener)
    }

    private fun checkItemCount(){
        var uid = auth.currentUser?.uid.toString()
        database = FirebaseDatabase.getInstance().getReference("Cart")
        var cartRef = database.child(uid).child(storeID)

        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var itemTotal = snapshot.childrenCount
                binding.totalInCartTxt.text = itemTotal.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        }
        cartRef.addListenerForSingleValueEvent(postListener)
    }

    private fun getUserLatLon(){
        var uid = auth.currentUser?.uid.toString()
        database = FirebaseDatabase.getInstance().getReference("Users")
        var getUserRef = database.child(uid)

        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var lat = snapshot.child("lat").value.toString()
                var lon = snapshot.child("lon").value.toString()

                recyclerview(lat.toDouble(),lon.toDouble())
                checkCartExist()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        getUserRef.addListenerForSingleValueEvent(postListener)
    }

    private fun recyclerview(userLat:Double, userLon:Double){
        val progress = ProgressDialog(activity)
        progress.setTitle("Loading Stores, Please wait")
        progress.show()
        var storeArrayList = ArrayList<StoreListData>()
        database = FirebaseDatabase.getInstance().getReference("Stores")

        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                storeArrayList.clear()
                var i = 0
                snapshot.children.forEach{
                    var position = i++
                    var storeImage = it.child("storeImage").getValue().toString()
                    var storeName = it.child("storeName").getValue().toString()
                    var startTime = it.child("startTime").getValue().toString()
                    var endTime = it.child("endTime").getValue().toString()
                    var operatingDays = it.child("operatingDay").getValue().toString()
                    var storeLat = it.child("lat").getValue().toString().toDouble()
                    var storeLon = it.child("lon").getValue().toString().toDouble()

                    val sdf = SimpleDateFormat("HH:mm")
                    val currentDate = sdf.format(Date())
                    val checkCurrentTime = SimpleDateFormat("HH:mm").parse(currentDate)
                    val checkOpeningTime = SimpleDateFormat("HH:mm").parse(startTime)
                    val checkEndingTime = SimpleDateFormat("HH:mm").parse(endTime)
                    var storeStatus = ""
                    var distance : Float ?= null
                    val result = FloatArray(5)

//                    Log.d("Location",storeLat.toString() +  " + " + storeLon.toString())

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


                 var sortedStoreList = storeArrayList.sortedWith(compareBy{it.city})


                val recyclerView = binding.recyclerviewStores
                recyclerView.layoutManager = LinearLayoutManager(context)

                recyclerView.adapter = StoreListAdapter(sortedStoreList){ StoreListData,position ->
                            var bundle = bundleOf(
                                "storename" to sortedStoreList[position].storeName,
                                "storeStatus" to sortedStoreList[position].status,
                            )
                            findNavController().navigate(R.id.action_homePage_to_storeDetailPage,bundle)
                }
                progress.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("FrontPage",error.toString())
                Toast.makeText(context,error.toString(),Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        }
        database.addValueEventListener(postListener)
    }
}