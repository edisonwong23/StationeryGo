package com.example.stationerygo.StorePage

import android.app.ProgressDialog
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
import java.util.*
import kotlin.collections.ArrayList

private lateinit var binding: FragmentStoreListBinding
private lateinit var database: DatabaseReference

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
        recyclerview()

        binding.goToSearchBtn.setOnClickListener {
//            findNavController().navigate(R.id.action_homePage_to_mapViewTesting)
            findNavController().navigate(R.id.action_homePage_to_registerAddressPage)
//              findNavController().navigate(R.id.action_homePage_to_userAddressPage)
        }


        return binding.root
    }

    private fun recyclerview(){
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
                    var city = it.child("state").getValue().toString()
                    var startTime = it.child("startTime").getValue().toString()
                    var endTime = it.child("endTime").getValue().toString()

                    val sdf = SimpleDateFormat("HH:mm")
                    val currentDate = sdf.format(Date())
                    val checkCurrentTime = SimpleDateFormat("HH:mm").parse(currentDate)
                    val checkOpeningTime = SimpleDateFormat("HH:mm").parse(startTime)
                    val checkEndingTime = SimpleDateFormat("HH:mm").parse(endTime)
                    var storeStatus = ""

                    if(checkCurrentTime.after(checkOpeningTime) && checkCurrentTime.before(checkEndingTime)){
                        storeStatus = "Open"
                    }
                    else
                        storeStatus = "Close"

                    storeArrayList.add(StoreListData(position,storeImage,storeName,city,startTime,endTime,storeStatus))
                }

                val recyclerView = binding.recyclerviewStores
                recyclerView.layoutManager = LinearLayoutManager(context)

                recyclerView.adapter = StoreListAdapter(storeArrayList){ StoreListData,position ->
                            var bundle = bundleOf(
                                "storename" to storeArrayList[position].storeName
                            )
                            findNavController().navigate(R.id.action_homePage_to_storeDetailPage,bundle)
                }
                progress.hide()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("FrontPage",error.toString())
                Toast.makeText(context,error.toString(),Toast.LENGTH_SHORT).show()
                progress.hide()
            }
        }
        database.addValueEventListener(postListener)
    }
}