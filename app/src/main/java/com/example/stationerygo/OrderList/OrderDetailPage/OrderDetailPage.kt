package com.example.stationerygo.OrderList.OrderDetailPage

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
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
import com.example.stationerygo.OrderList.OrderListAdapter
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
        getOrderRecyclerView()
        binding.orderDetailsCancelOrderBtn.setOnClickListener{
            deleteOrder()
        }

        return binding.root
    }

    private fun deleteOrder(){
        val alartDialog = AlertDialog.Builder(context,R.style.AlertDialogCustom)

        alartDialog.apply {
            setTitle("Delete Order?")
            setMessage("Are you sure you want to delete this order?")
            setPositiveButton("Clear"){ _: DialogInterface?, _: Int ->
                deleteOrderFromFirebase()
            }
            setNegativeButton("Cancel"){_, _ ->
                Toast.makeText(context,"Action has been cancelled.",Toast.LENGTH_SHORT).show()
            }
        }.create().show()

    }

    private fun deleteOrderFromFirebase(){
        val orderID = arguments?.getString("orderID").toString()
        val orderKey = arguments?.getString("orderKey").toString()
        database = FirebaseDatabase.getInstance().getReference("Orders")

        database.child(orderKey).removeValue().addOnCompleteListener{
            Toast.makeText(context,"Order has been Deleted",Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }.addOnFailureListener{
            Log.d("Orders", it.toString())
            Toast.makeText(context,"Fail to Delete Order",Toast.LENGTH_SHORT).show()
        }

    }

    private fun getOrderRecyclerView(){
        val orderID = arguments?.getString("orderID").toString()
        database = FirebaseDatabase.getInstance().getReference("Orders")

        var itemArrayList = ArrayList<OrderDetailsClass>()

        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                itemArrayList.clear()
                snapshot.children.forEach{
                    var checkOrderID = it.child("orderID").value?.equals(orderID)
                    if(checkOrderID == true){
                        it.child("itemOrdered").children.forEach{
                            var itemImage = it.child("itemImage").value.toString()
                            var itemName = it.child("itemName").value.toString()
                            var itemPrice = it.child("itemPrice").value.toString()
                            var itemQty = it.child("itemQty").value.toString()
                            itemArrayList.add(OrderDetailsClass(itemImage,itemName,itemPrice,itemQty))
                        }
                    }
                }

                val recyclerView = binding.recyclerView
                recyclerView.layoutManager = LinearLayoutManager(context)

                recyclerView.adapter = OrderDetailsAdapter(itemArrayList){ OrderDetailsClass, position:Int ->
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        database.addListenerForSingleValueEvent(postListener)
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
                var orderPaymentType = ""
                var orderPaymentAmount = ""
                var orderAddress = ""
                var orderType = ""

                snapshot.children.forEach{
                    var orderTrue = it.child("orderID").value?.equals(orderID)
//                    Log.d("Orders",orderTrue.toString())
                    if(orderTrue == true){
                        orderStatus = it.child("orderStatus").value.toString()
                        orderDate = it.child("purchaseDate").value.toString()
                        orderPaymentType = it.child("paymentType").value.toString()
                        orderPaymentAmount = it.child("totalAmount").value.toString()
                        orderAddress = it.child("address").value.toString()
                        orderType = it.child("orderType").value.toString()
                    }
                }
                binding.orderDetailsShopNameTxt.text = shopName
                binding.orderDetailsStatusTxt.text = orderStatus
                binding.orderDetailsPurchaseDateTxt.text = orderDate
                binding.orderDetailsPaymentTypeTxt.text = orderPaymentType
                binding.orderDetailsPaymentAmountTxt.text = "RM "+orderPaymentAmount
                binding.orderDetailsOrderTypeTxt.text = orderType

                if(orderStatus == "Completed"){
                    binding.orderDetailsStatusTxt.setTextColor(Color.parseColor("#689f38"))
                }
                else if(orderStatus == "Pending"){
                    binding.orderDetailsStatusTxt.setTextColor(Color.parseColor("#000000"))
                }
                else if(orderStatus == "Cancel"){
                    binding.orderDetailsStatusTxt.setTextColor(Color.parseColor("#d50000"))
                }
                else{
                    binding.orderDetailsStatusTxt.setTextColor(Color.parseColor("#6200EE"))
                }

                if(orderType == "Delivery"){
                    binding.orderDetailsAddressCard.visibility = View.VISIBLE
                    binding.orderDetailsAddressTxt.text = orderAddress
                }
                else{
                    binding.orderDetailsAddressCard.visibility = View.GONE
                }


                if(orderStatus != "Pending"){
                    binding.orderDetailsCancelOrderBtn.visibility = View.GONE
                }else{
                    binding.orderDetailsCancelOrderBtn.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        }

        dataRef.addListenerForSingleValueEvent(postListener)
    }

}