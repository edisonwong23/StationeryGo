package com.example.stationerygo.StoreOwner.OrderPage

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
import com.example.stationerygo.databinding.FragmentMainStorePageBinding
import com.example.stationerygo.databinding.FragmentShopOrderPageBinding
import com.google.firebase.database.*

private lateinit var binding: FragmentShopOrderPageBinding
private lateinit var database: DatabaseReference


class ShopOrderPage : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

       binding = FragmentShopOrderPageBinding.inflate(
            inflater,
            container,
            false
        )
        loadRecyclerOrder()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun loadRecyclerOrder(){
        var storeID = arguments?.getString("storeID").toString()
//        Log.d("Orders",storeID)
        database = FirebaseDatabase.getInstance().getReference("Orders")
        val dataRef = database

        var userID : MutableList<String> = ArrayList()
        var orderStatus : MutableList<String> = ArrayList()
        var orderDate : MutableList<String> = ArrayList()
        var orderID : MutableList<String> = ArrayList()
        var orderKey : MutableList<String> = ArrayList()

        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                userID.clear()
                orderStatus.clear()
                orderDate.clear()
                orderID.clear()
                orderKey.clear()
                snapshot.children.forEach {
//                    Log.d("Orders",it.value.toString())
                    if(it.child("storeID").value!!.equals(storeID)){
                         userID.add(it.child("userID").value.toString())
                         orderStatus.add(it.child("orderStatus").value.toString())
                         orderDate.add(it.child("purchaseDate").value.toString())
                         orderID.add(it.child("orderID").value.toString())
                        orderKey.add(it.key.toString())
                         loadUserInfo(userID,orderStatus,orderDate,orderID,orderKey)
//                        Log.d("Orders",orderID.toString())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,"Error getting data",Toast.LENGTH_SHORT).show()
            }

        }

        dataRef.addValueEventListener(postListener)
    }


    private fun loadUserInfo(userID:MutableList<String>,
                             orderStatus:MutableList<String>,
                             orderDate:MutableList<String>,
                             orderID:MutableList<String>,
                             orderKey:MutableList<String>){
        database = FirebaseDatabase.getInstance().getReference("Users")

        val dataRef = database

        var userDisplayName : MutableList<String> = ArrayList()
        var userImg : MutableList<String> = ArrayList()
        var displayOrderList = ArrayList<ShopOrderData>()
        var i = 0
        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                displayOrderList.clear()
                for(data in orderID){
//                      Log.d("Orders","UserID: " + userID[i])
//                    Log.d("Orders","UserID " + snapshot.child(userID[i]).value.toString())
                     userDisplayName.add(snapshot.child(userID[i]).child("displayUsername").value.toString())
                     userImg.add(snapshot.child(userID[i]).child("userImage").value.toString())
                     displayOrderList.add(ShopOrderData(orderID[i],orderDate[i],orderStatus[i],userImg[i],userDisplayName[i]))
                     i++
                }


                var recyclerView = binding.shopOrderRecyclerList
                recyclerView.layoutManager = LinearLayoutManager(context)

                recyclerView.adapter = ShopOrderAdapter(displayOrderList){ StoreListData, position ->
                    var bundle = bundleOf(
                        "orderKey" to orderKey[position],
                        "userID" to userID[position]
                    )
                    findNavController().navigate(R.id.action_shopOrderPage_to_shopOrderDetails,bundle)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,"Error getting data",Toast.LENGTH_SHORT).show()
            }

        }

        dataRef.addValueEventListener(postListener)
    }
}