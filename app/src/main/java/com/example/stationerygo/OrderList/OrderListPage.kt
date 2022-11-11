package com.example.stationerygo.OrderList

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
import com.example.stationerygo.Cart.CartAdapet
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentOrderListBinding
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
                        shopID?.add(it.child("storeID").value.toString())
                        orderID?.add(it.child("orderID").value.toString())
                        orderDate?.add(it.child("purchaseDate").value.toString())
                        orderStatus?.add(it.child("orderStatus").value.toString())
//                        Log.d("Orders", "OrderID[$i]: " + orderID[i])
//                        Log.d("Orders", "OrderID: "+ it.child("orderID").value.toString())
                        i++
                    }



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

                    orderList.add(OrderListData(orderID[i],shopName[i],orderDate[i],orderStatus[i],shopImage[i]))
                    i++
                }

                var recyclerView = binding.orderRecyclerView
                recyclerView.layoutManager = LinearLayoutManager(context)

                recyclerView.adapter = OrderListAdapter(orderList){ OrderListData, position:Int ->
                    var bundle = bundleOf(
                        "orderID" to orderID[position],
                        "shopName" to shopName[position],
                        "orderKey" to orderKey[position],
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
        database.addListenerForSingleValueEvent(postListener)

    }

}