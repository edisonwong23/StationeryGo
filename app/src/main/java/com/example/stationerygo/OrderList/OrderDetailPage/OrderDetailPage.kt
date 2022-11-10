package com.example.stationerygo.OrderList.OrderDetailPage

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentOrderDetailPageBinding
import com.example.stationerygo.databinding.FragmentOrderListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

private lateinit var binding: FragmentOrderDetailPageBinding
private lateinit var database: DatabaseReference

class OrderDetailPage : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

            binding = FragmentOrderDetailPageBinding.inflate(
            inflater,
            container,
            false
        )

        getOrderDetails()

        return binding.root
    }

    private fun getOrderDetails(){
        val orderID = arguments?.getString("orderID").toString()
        val shopName = arguments?.getString("shopName").toString()
        database = FirebaseDatabase.getInstance().getReference("Orders")
        val dataRef = database

        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var orderStatus = ""
                var orderDate = ""

                snapshot.children.forEach{
                    var orderTrue = it.child("orderID").value?.equals(orderID)
//                    Log.d("Orders",orderTrue.toString())
                    if(orderTrue == true){
                        orderStatus = it.child("orderStatus").value.toString()
                        orderDate = it.child("purchaseDate").value.toString()
                        Log.d("Orders",orderStatus)
                        Log.d("Orders",orderDate)
                    }
                }
                binding.orderDetailsShopNameTxt.text = shopName
                binding.orderDetailsStatusTxt.text = orderStatus
                binding.orderDetailsPurchaseDateTxt.text = orderDate
            }

            override fun onCancelled(error: DatabaseError) {

            }

        }

        dataRef.addListenerForSingleValueEvent(postListener)
    }

}