package com.example.stationerygo.OrderList

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.stationerygo.Cart.Cart_Data
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentOrderListBinding
import com.example.stationerygo.databinding.FragmentUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

private lateinit var binding : FragmentOrderListBinding
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth

class OrderListPage : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderListBinding.inflate(
            inflater,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()
        getOrders()

        return binding.root
    }

    private fun getOrders(){
        var uid = auth.currentUser?.uid.toString()

        database = FirebaseDatabase.getInstance().getReference("Orders")
        var dataRef = database

        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var itemID : String = ""
                var num = 0
                snapshot.children.forEach{
                    var i = 0
                    var items = ArrayList<Cart_Data>()
                    num = it.child("itemOrdered").childrenCount.toInt()
                    it.child("itemOrdered").children.forEach{
                        itemID = it.child("itemName").value.toString()
                        Log.d("Orders","ItemName[$i]: $itemID \n")
                        i++
                    }

//                    Log.d("Orders",it.child("userID").value.toString()+ "\n")
//                    Log.d("Orders",it.toString()+ "\n")
                }
                Log.d("Orders","Item Count: $num \n")

                var shopID = snapshot.child("storeID").value.toString()
                var paymentType = snapshot.child("paymentType").value.toString()

//               Log.d("Orders","StoreID: $shopID -- PaymentType: $paymentType \n")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        dataRef.addValueEventListener(postListener)
    }

}