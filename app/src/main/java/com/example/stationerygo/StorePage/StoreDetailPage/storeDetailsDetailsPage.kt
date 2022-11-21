package com.example.stationerygo.StorePage.StoreDetailPage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentStoreDetailPageBinding
import com.example.stationerygo.databinding.FragmentStoreDetailsDetailsPageBinding
import com.google.firebase.database.*

private lateinit var binding: FragmentStoreDetailsDetailsPageBinding
private lateinit var database: DatabaseReference
private var intentPhone = ""
private var intentEmail = ""
private var shopLat = ""
private var shopLon = ""


class storeDetailsDetailsPage : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentStoreDetailsDetailsPageBinding.inflate(
            inflater,
            container,
            false
        )

        binding.storeDetailsDetailsContactPhoneTxttxt.setOnClickListener{
            val intent = Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+ intentPhone))
            startActivity(intent)
        }

        binding.storeDetailsDetailsContactEmailTxttxt.setOnClickListener{
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.setData(Uri.parse("mailto:"+ intentEmail))
            startActivity(intent)
        }

        binding.checkLocationMapBtn.setOnClickListener {
            val gmmIntentUri = Uri.parse("google.streetview:cbll= $shopLat,$shopLon")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

        loadFromDatabase()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun loadFromDatabase(){
        var storeName = arguments?.getString("storeName").toString()
        database = FirebaseDatabase.getInstance().getReference("Stores")
        var dataRef = database.orderByChild("storeName").equalTo(storeName)

        val postListener = object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                snapshot.children.forEach {
                    var desc = it.child("description").value.toString()
                    var startTime = it.child("startTime").value.toString()
                    var endTime = it.child("endTime").value.toString()
                    var days = it.child("operatingDay").value.toString()
                    var email = it.child("email").value.toString()
                    var phone = it.child("phone").value.toString()
                    var address = it.child("address").value.toString()
                    var lat = it.child("lat").value.toString()
                    var lon = it.child("lon").value.toString()

                    shopLat = lat
                    shopLon = lon

                    intentPhone = phone
                    intentEmail = email

                    days = days.replace(",", " , ")

                    binding.storeDetailDetailShopNameTxt.text = storeName
                    binding.storeDetailsDetailsDescTxt.text = desc
                    binding.storeDetailsDetailsOperatingTimeTxt.text = startTime + " - " + endTime
                    binding.storeDetailsDetailsOperatingDayTxt.text = days
                    binding.storeDetailsDetailsContactEmailTxttxt.text = email
                    binding.storeDetailsDetailsContactPhoneTxttxt.text = phone
                    binding.storeDetailsDetailsAddressTxttxt.text = address
//                    binding.storeDetailsDetailsAddressCityTxttxt.text = city
//                    binding.storeDetailsDetailsAddressPostalTxttxt.text = postal


                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        dataRef.addListenerForSingleValueEvent(postListener)
    }

}