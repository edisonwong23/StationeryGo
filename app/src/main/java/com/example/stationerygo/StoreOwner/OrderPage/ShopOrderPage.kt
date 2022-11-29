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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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

        binding.shopOrderAllOrderBtn.setOnClickListener {
            binding.shopAllOrderOrderActionCard.visibility = View.VISIBLE
            binding.shopOrderRecyclerListCard.visibility = View.GONE
        }

        binding.shopOrderCurrentOrderBtn.setOnClickListener {
            binding.shopAllOrderOrderActionCard.visibility = View.GONE
            binding.shopOrderRecyclerListCard.visibility = View.VISIBLE
        }

        loadRecyclerOrder()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun loadRecyclerOrder(){
        var storeID = arguments?.getString("storeID").toString()
//        Log.d("Orders",storeID)
        database = FirebaseDatabase.getInstance().getReference("Orders")

        var userID : MutableList<String> = ArrayList()
        var orderStatus : MutableList<String> = ArrayList()
        var orderDate : MutableList<String> = ArrayList()
        var orderID : MutableList<String> = ArrayList()
        var orderKey : MutableList<String> = ArrayList()
        var orderType : MutableList<String> = ArrayList()
        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                userID.clear()
                orderStatus.clear()
                orderDate.clear()
                orderID.clear()
                orderKey.clear()
                orderType.clear()

                snapshot.children.forEach {
//                    Log.d("Orders",it.value.toString())
                    if(it.child("storeID").value!!.equals(storeID)){


                            userID.add(it.child("userID").value.toString())
                            orderStatus.add(it.child("orderStatus").value.toString())
                            orderDate.add(it.child("purchaseDate").value.toString())
                            orderID.add(it.child("orderID").value.toString())
                            orderType.add(it.child("orderType").value.toString())
                            orderKey.add(it.key.toString())
                            loadUserInfo(userID,orderStatus,orderDate,orderID,orderKey,orderType)

//                        Log.d("Orders",orderID.toString())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,"Error getting data",Toast.LENGTH_SHORT).show()
            }

        }

        database.addValueEventListener(postListener)
    }


    private fun loadUserInfo(userID:MutableList<String>,
                             orderStatus:MutableList<String>,
                             orderDate:MutableList<String>,
                             orderID:MutableList<String>,
                             orderKey:MutableList<String>,
                             orderType:MutableList<String>){

        database = FirebaseDatabase.getInstance().getReference("Users")

        val dataRef = database

        var userDisplayName : MutableList<String> = ArrayList()
        var userImg : MutableList<String> = ArrayList()
        var displayOrderList = ArrayList<ShopOrderData>()
        var displayCompletedList = ArrayList<ShopOrderData>()

        var i = 0

        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
//                displayOrderList.clear()
//                displayCompletedList.clear()
//                userDisplayName.clear()
//                userImg.clear()
                for(i in 0 until orderID.size){
//                      Log.d("Orders","UserID: " + userID[i])
//                    Log.d("Orders","UserID " + snapshot.child(userID[i]).value.toString())
                     userDisplayName.add(snapshot.child(userID[i]).child("displayUsername").value.toString())
                     userImg.add(snapshot.child(userID[i]).child("userImage").value.toString())

                    if(orderStatus[i] == "Completed" || orderStatus[i] == "Cancel"){
//                        Log.d("Orders","$i " + orderStatus[i])
                        displayCompletedList.add(ShopOrderData(orderKey[i],orderID[i],orderDate[i],orderStatus[i],userImg[i],userDisplayName[i],userID[i],orderType[i]))
                    }
                    else{
//                        Log.d("Orders","$i " + orderStatus[i])
                        displayOrderList.add(ShopOrderData(orderKey[i],orderID[i],orderDate[i],orderStatus[i],userImg[i],userDisplayName[i],userID[i],orderType[i]))
                    }
//                    i++
                }

                fun String.toDate(): Date {
                    return SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).parse(this)
                }

                var displayOrderListSorted = displayOrderList.sortedByDescending { it.orderDate?.toDate() }
                var displayCompletedListSorted = displayCompletedList.sortedByDescending { it.orderDate?.toDate() }

                var recyclerView = binding.shopOrderRecyclerList
                recyclerView.layoutManager = LinearLayoutManager(context)

                recyclerView.adapter = ShopOrderAdapter(displayOrderListSorted){ StoreListData, position ->
                    var bundle = bundleOf(
                        "orderKey" to displayOrderListSorted[position].orderKey,
                        "userID" to displayOrderListSorted[position].userID
                    )
                    findNavController().navigate(R.id.action_shopOrderPage_to_shopOrderDetails,bundle)
                }

                var recyclerViewCompleted = binding.shopAllOrderRecyclerList
                recyclerViewCompleted.layoutManager = LinearLayoutManager(context)

                recyclerViewCompleted.adapter = ShopOrderAdapter(displayCompletedListSorted){ StoreListData, position ->
                    var bundle = bundleOf(
                        "orderKey" to displayCompletedListSorted[position].orderKey,
                        "userID" to displayCompletedListSorted[position].userID
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