package com.example.stationerygo.StorePage.StoreDetailPage

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
                    var city = it.child("state").value.toString()
                    var postal = it.child("postal").value.toString()

                    days = days.replace(",", " , ")

                    binding.storeDetailDetailShopNameTxt.text = storeName
                    binding.storeDetailsDetailsDescTxt.text = desc
                    binding.storeDetailsDetailsOperatingTimeTxt.text = startTime + " - " + endTime
                    binding.storeDetailsDetailsOperatingDayTxt.text = days
                    binding.storeDetailsDetailsContactEmailTxttxt.text = email
                    binding.storeDetailsDetailsContactPhoneTxttxt.text = phone
                    binding.storeDetailsDetailsAddressTxttxt.text = address
                    binding.storeDetailsDetailsAddressCityTxttxt.text = city
                    binding.storeDetailsDetailsAddressPostalTxttxt.text = postal
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        dataRef.addListenerForSingleValueEvent(postListener)
    }

}