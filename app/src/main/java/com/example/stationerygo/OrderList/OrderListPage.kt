package com.example.stationerygo.OrderList

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stationerygo.Cart.CartAdapet
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentOrderListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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

        binding.orderCurrentOrderBtn.setOnClickListener {
            binding.orderCurrentOrderCard.visibility = View.VISIBLE
            binding.orderAllRecyclerViewCard.visibility = View.GONE
            getCurrentOrders()
        }

        binding.orderAllOrderBtn.setOnClickListener {
            binding.orderCurrentOrderCard.visibility = View.GONE
            binding.orderAllRecyclerViewCard.visibility = View.VISIBLE
            getAllOrders()
        }
        getCurrentOrders()
        getAllOrders()

        return binding.root
    }


    private fun getCurrentOrders(){
        var uid = auth.currentUser?.uid.toString()

        database = FirebaseDatabase.getInstance().getReference("Orders")
        var dataRef = database

        val postListener = object : ValueEventListener{
            var shopID : MutableList<String> = ArrayList()
            var orderID : MutableList<String> = ArrayList()
            var orderDate : MutableList<String> = ArrayList()
            var orderStatus : MutableList<String> = ArrayList()
            var orderKey : MutableList<String> = ArrayList()

            override fun onDataChange(snapshot: DataSnapshot) {
                shopID.clear()
                orderID.clear()
                orderDate.clear()
                orderStatus.clear()
                var i = 0
                snapshot.children.forEach{
                    var getUserOrders = it.child("userID").value?.equals(uid)
                    if(getUserOrders == true){
                        orderKey.add(it.key.toString())
//                        Log.d("Orders", it.child("orderID").value.toString())
                        var currentStatus = it.child("orderStatus").value.toString()
                        if(!currentStatus.equals("Completed") && !currentStatus.equals("Cancel"))
                        {
                            shopID?.add(it.child("storeID").value.toString())
                            orderID?.add(it.child("orderID").value.toString())
                            orderDate?.add(it.child("purchaseDate").value.toString())
                            orderStatus?.add(it.child("orderStatus").value.toString())
                        }

//                        Log.d("Orders", "OrderID[$i]: " + orderID[i])
//                        Log.d("Orders", "OrderID: "+ it.child("orderID").value.toString())
                        i++

                    }

                }
                if(orderID.count() >= 1){
                    binding.orderCurrentOrderCard.visibility = View.VISIBLE
                    binding.imageView5.visibility = View.INVISIBLE
                    binding.textView6.visibility = View.INVISIBLE
                }
                else{
                    binding.orderCurrentOrderCard.visibility = View.INVISIBLE
                    binding.imageView5.visibility = View.VISIBLE
                    binding.textView6.visibility = View.VISIBLE
                }
//                Log.d("Orders", "OrderID: $orderID")
                getShopNameImage(shopID,orderID,orderDate,orderStatus,orderKey)

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        dataRef.addValueEventListener(postListener)
    }

    private fun getAllOrders(){
        var uid = auth.currentUser?.uid.toString()

        database = FirebaseDatabase.getInstance().getReference("Orders")
        var dataRef = database

        val postListener = object : ValueEventListener{
            var shopID : MutableList<String> = ArrayList()
            var orderID : MutableList<String> = ArrayList()
            var orderDate : MutableList<String> = ArrayList()
            var orderStatus : MutableList<String> = ArrayList()
            var orderKey : MutableList<String> = ArrayList()

            override fun onDataChange(snapshot: DataSnapshot) {
                shopID.clear()
                orderID.clear()
                orderDate.clear()
                orderStatus.clear()
                var i = 0
                snapshot.children.forEach{
                    var getUserOrders = it.child("userID").value?.equals(uid)
                    if(getUserOrders == true){
                        var currentOrderStatus = it.child("orderStatus").value.toString()
                        if(currentOrderStatus.equals("Completed") || currentOrderStatus.equals("Cancel")){
                            orderKey.add(it.key.toString())
                            shopID?.add(it.child("storeID").value.toString())
                            orderID?.add(it.child("orderID").value.toString())
                            orderDate?.add(it.child("purchaseDate").value.toString())
                            orderStatus?.add(it.child("orderStatus").value.toString())
                        }
                        i++
                    }
                }

                if(orderID.count() >= 1){
//                    binding.orderAllRecyclerViewCard.visibility = View.GONE
                    binding.imageView5.visibility = View.INVISIBLE
                    binding.textView6.visibility = View.INVISIBLE
                }
                else{
                    binding.orderAllRecyclerViewCard.visibility = View.GONE
                    binding.imageView5.visibility = View.VISIBLE
                    binding.textView6.visibility = View.VISIBLE
                }
                getAllShopNameImage(shopID,orderID,orderDate,orderStatus,orderKey)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        dataRef.addValueEventListener(postListener)
    }

    private fun getShopNameImage(shopID : MutableList<String>,
                                 orderID : MutableList<String>,
                                 orderDate : MutableList<String>,
                                 orderStatus : MutableList<String>,
                                 orderKey: MutableList<String>) {
        database = FirebaseDatabase.getInstance().getReference("Stores")
        var orderList = ArrayList<OrderListData>()
        var shopName: MutableList<String> = ArrayList()
        var shopImage: MutableList<String> = ArrayList()
        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                orderList.clear()
                var i = 0
                for (data in orderID){
                    snapshot.children.forEach{
                        var getShop = it.child("storeID").value?.equals(shopID[i])
                        if(getShop == true){
                            shopName.add(it.child("storeName").value.toString())
                            shopImage.add(it.child("storeImage").value.toString())
                        }
                    }

                    orderList.add(OrderListData(orderID[i],shopName[i],orderDate[i],orderStatus[i],shopImage[i],orderKey[i]))
                    i++
                }

                var recyclerView = binding.orderCurrentRecyclerView
                recyclerView.layoutManager = LinearLayoutManager(context)

                fun String.toDate(): Date {
                    return SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).parse(this)
                }
                val sortedCurrentOrder = orderList.sortedByDescending { it.orderDate?.toDate() }

                FirebaseMessaging.getInstance().token.addOnCompleteListener{task ->
                    if (!task.isSuccessful) {
                        Log.w("Order", "Fetching FCM registration token failed", task.exception)
                    }

                    // Get new FCM registration token
                    val token = task.result

                    // Log and toast
                    Log.d("Order", token.toString())

                }


                recyclerView.adapter = OrderListAdapter(sortedCurrentOrder){ OrderListData, position:Int ->
                    var bundle = bundleOf(
                        "orderID" to sortedCurrentOrder[position].orderID,
                        "shopName" to sortedCurrentOrder[position].orderShop,
                        "orderKey" to sortedCurrentOrder[position].orderKey,
                    )

                    findNavController().navigate(R.id.action_homePage_to_orderDetailPage,bundle)
                }

//                Log.d("Orders",orderID)
//                orderList.add(OrderListData(orderID,shopName,orderDate,orderStatus,shopImage))

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        database.addValueEventListener(postListener)

    }

    private fun getAllShopNameImage(shopID : MutableList<String>,
                                 orderID : MutableList<String>,
                                 orderDate : MutableList<String>,
                                 orderStatus : MutableList<String>,
                                 orderKey: MutableList<String>) {
        database = FirebaseDatabase.getInstance().getReference("Stores")
        var orderList = ArrayList<OrderListData>()
        var shopName: MutableList<String> = ArrayList()
        var shopImage: MutableList<String> = ArrayList()
        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                orderList.clear()
                var i = 0
                for (data in orderID){
                    snapshot.children.forEach{
                        var getShop = it.child("storeID").value?.equals(shopID[i])
                        if(getShop == true){
                            shopName.add(it.child("storeName").value.toString())
                            shopImage.add(it.child("storeImage").value.toString())
                        }
                    }

                    orderList.add(OrderListData(orderID[i],shopName[i],orderDate[i],orderStatus[i],shopImage[i],orderKey[i]))

                    i++
                }

                var recyclerView = binding.orderAllRecyclerView
                recyclerView.layoutManager = LinearLayoutManager(context)

                fun String.toDate(): Date {
                    return SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).parse(this)
                }

                var sortedList = orderList.sortedByDescending { it.orderDate?.toDate() }

                recyclerView.adapter = OrderListAdapter(sortedList){ OrderListData, position:Int ->
                    var bundle = bundleOf(
                        "orderID" to sortedList[position].orderID,
                        "shopName" to sortedList[position].orderShop,
                        "orderKey" to sortedList[position].orderKey,
                    )
                    findNavController().navigate(R.id.action_homePage_to_orderDetailPage,bundle)
                }

//                Log.d("Orders",orderID)
//                orderList.add(OrderListData(orderID,shopName,orderDate,orderStatus,shopImage))

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        database.addValueEventListener(postListener)

    }



}