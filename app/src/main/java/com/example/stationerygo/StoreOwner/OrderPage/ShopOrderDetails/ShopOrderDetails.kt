package com.example.stationerygo.StoreOwner.OrderPage.ShopOrderDetails

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.core.snap
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stationerygo.Cart.Payment.PaymentData
import com.example.stationerygo.OrderList.OrderDetailPage.OrderDetailsAdapter
import com.example.stationerygo.OrderList.OrderDetailPage.OrderDetailsClass
import com.example.stationerygo.R
import com.example.stationerygo.StoreOwner.OrderPage.ShopOrderAdapter
import com.example.stationerygo.databinding.FragmentShopOrderDetailsBinding
import com.example.stationerygo.databinding.FragmentShopOrderPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

private lateinit var binding: FragmentShopOrderDetailsBinding
private lateinit var database: DatabaseReference
private lateinit var auth : FirebaseAuth
private var intentPhone = ""
private var intentEmail = ""

class ShopOrderDetails : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShopOrderDetailsBinding.inflate(
            inflater,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()

        loadUserDate()

        binding.shopOrderDetailsPhoneTxt.setOnClickListener{
            val intent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:"+ intentPhone))
            startActivity(intent)
        }

        binding.shopOrderDetailsEmailTxt.setOnClickListener{
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.setData(Uri.parse("mailto:"+ intentEmail))
            startActivity(intent)
        }

        binding.shopOrderDetailsCompleteOrderBtn.setOnClickListener{
            val alartDialog = AlertDialog.Builder(context,R.style.AlertDialogCustom)
            alartDialog.apply {
                setTitle("Complete Order?")
                setMessage("Are you sure you want to complete order?")
                setPositiveButton("Complete"){ _: DialogInterface?, _: Int ->
                    completeOrder()
                }
                setNegativeButton("Cancel"){_, _ ->
                }
            }.create().show()
        }

        binding.shopOrderDetailsCancelOrderBtn.setOnClickListener {
            val alartDialog = AlertDialog.Builder(context,R.style.AlertDialogCustom)
            alartDialog.apply {
                setTitle("Cancel Order?")
                setMessage("Are you sure you want to cancel order?")
                setPositiveButton("Confirm"){ _: DialogInterface?, _: Int ->
                    cancelOrder()
                }
                setNegativeButton("Cancel"){_, _ ->
                }
            }.create().show()
        }

        binding.shopOrderDetailsAccepOrderBtn.setOnClickListener {
            acceptOrder()
        }

        binding.shopOrderDetailsUncancelOrderBtn.setOnClickListener {
            unCancelOrder()
        }

        binding.ShopOrderDetailsDeliveryOrderBtn.setOnClickListener {

            val alartDialog = AlertDialog.Builder(context,R.style.AlertDialogCustom)
            alartDialog.apply {
                setTitle("Deliver Order?")
                setMessage("Start Delivering Order?")
                setPositiveButton("Deliver"){ _: DialogInterface?, _: Int ->
                    deliverOrder()
                }
                setNegativeButton("Cancel"){_, _ ->
                }
            }.create().show()
        }

        binding.readyForPickUpBtn.setOnClickListener {
            readyForPickUp()
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    private fun checkOrder(currentOrderStatus:String, currentOrderType:String){

        if(currentOrderStatus == "Preparing" && currentOrderType == "Pick Up"){
            binding.shopOrderDetailsCancelOrderBtn.visibility = View.GONE
            binding.shopOrderDetailsAccepOrderBtn.visibility = View.GONE
            binding.readyForPickUpBtn.visibility = View.VISIBLE
        }
        else if(currentOrderStatus == "Cancel"){
            binding.shopOrderDetailsCancelOrderBtn.visibility = View.GONE
            binding.shopOrderDetailsAccepOrderBtn.visibility = View.GONE
            binding.shopOrderDetailsUncancelOrderBtn.visibility = View.VISIBLE
        }
        else if(currentOrderStatus == "Completed"){
            binding.shopOrderDetailsCancelOrderBtn.visibility = View.GONE
            binding.shopOrderDetailsAccepOrderBtn.visibility = View.GONE
            binding.shopOrderDetailsCompleteOrderBtn.visibility = View.GONE
            binding.shopOrderDetailsUncancelOrderBtn.visibility = View.GONE
        }
        else if(currentOrderStatus == "Preparing" && currentOrderType == "Delivery"){
            binding.shopOrderDetailsCancelOrderBtn.visibility = View.GONE
            binding.shopOrderDetailsAccepOrderBtn.visibility = View.GONE
            binding.ShopOrderDetailsDeliveryOrderBtn.visibility = View.VISIBLE
        }
        else if(currentOrderStatus == "Delivering"){
            binding.shopOrderDetailsCancelOrderBtn.visibility = View.GONE
            binding.shopOrderDetailsAccepOrderBtn.visibility = View.GONE
            binding.ShopOrderDetailsDeliveryOrderBtn.visibility = View.GONE
            binding.shopOrderDetailsUncancelOrderBtn.visibility = View.GONE
            binding.shopOrderDetailsCompleteOrderBtn.visibility = View.VISIBLE
        }
        else if(currentOrderStatus == "Pick Up Ready"){
            binding.shopOrderDetailsCancelOrderBtn.visibility = View.GONE
            binding.shopOrderDetailsAccepOrderBtn.visibility = View.GONE
            binding.readyForPickUpBtn.visibility = View.GONE
            binding.shopOrderDetailsCompleteOrderBtn.visibility = View.VISIBLE
        }
        else if(currentOrderStatus == "Pending"){
            binding.shopOrderDetailsCancelOrderBtn.visibility = View.VISIBLE
            binding.shopOrderDetailsAccepOrderBtn.visibility = View.VISIBLE
            binding.shopOrderDetailsUncancelOrderBtn.visibility = View.GONE
        }
    }


    private fun loadUserDate(){
        val uid = arguments?.getString("userID").toString()
        database = FirebaseDatabase.getInstance().getReference("Users")
        val dataRef = database.child(uid)

        val postListener = object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var userImg = snapshot.child("userImage").value.toString()
                var displayName = snapshot.child("displayUsername").value.toString()
                var phone = snapshot.child("phone").value.toString()
                var email = snapshot.child("email").value.toString()

                Picasso.get()
                    .load(userImg)
                    .into(binding.shopOrderDetailsUserImage)

                intentPhone = phone
                intentEmail = email

                binding.shopOrderDetailsUsernameTxt.text = displayName
                binding.shopOrderDetailsPhoneTxt.text = phone
                binding.shopOrderDetailsEmailTxt.text = email

                loadStoreLocation()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        dataRef.addListenerForSingleValueEvent(postListener)
    }

    private fun loadStoreLocation(){
        var uid = auth.currentUser?.uid.toString()
        database = FirebaseDatabase.getInstance().getReference("Stores")
        var locateRef = database.child(uid)

        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var storeLat = snapshot.child("lat").value.toString().toDouble()
                var storeLon = snapshot.child("lon").value.toString().toDouble()

                loadPaymentDetails(storeLat,storeLon)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        locateRef.addListenerForSingleValueEvent(postListener)
    }

    private fun loadPaymentDetails(storeLat:Double,storeLon:Double){
        var orderKey = arguments?.getString("orderKey").toString()
        database = FirebaseDatabase.getInstance().getReference("Orders")
        val dataRef = database.child(orderKey)

        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var paymentType = snapshot.child("paymentType").value.toString()
                var purchaseDate = snapshot.child("purchaseDate").value.toString()
                var totalAmount = snapshot.child("totalAmount").value.toString()
                var orderType = snapshot.child("orderType").value.toString()
                var orderStatus = snapshot.child("orderStatus").value.toString()
                var address = snapshot.child("address").value.toString()
                var lat = snapshot.child("lat").value.toString()
                var lon = snapshot.child("lon").value.toString()

                var userLat = 0.00
                var userLon = 0.00
                if(lat == "None" || lon == "None"){
                    userLat = 0.00
                    userLon = 0.00
                    binding.shopOrderDetailsAddressCard.visibility = View.GONE
                }
                else{
                    userLat = lat.toDouble()
                    userLon = lon.toDouble()
                }


                var distance : Float ?= null
                val result = FloatArray(5)

                Location.distanceBetween(userLat,userLon,storeLat,storeLon,result)
//                Log.d("Location",result[0].toString())
                distance = result[0] / 1000

                binding.shopOrderDetailsPaymentTypeTxt.text = paymentType
                binding.shopOrderDetailsOrderTypeTxt.text = orderType
                binding.shopOrderDetailsTotalAmountTxt.text = "RM "+totalAmount
                binding.shopOrderDetailsPaymentDateTxt.text = purchaseDate
                binding.shopOrderDetailsStatusTxt.text = orderStatus

                if(orderStatus == "Completed"){
                    binding.shopOrderDetailsStatusTxt.setTextColor(Color.parseColor("#689f38"))
                }
                else if(orderStatus == "Pending"){
                    binding.shopOrderDetailsStatusTxt.setTextColor(Color.parseColor("#000000"))
                }
                else if(orderStatus == "Cancel"){
                    binding.shopOrderDetailsStatusTxt.setTextColor(Color.parseColor("#d50000"))
                }
                else{
                    binding.shopOrderDetailsStatusTxt.setTextColor(Color.parseColor("#6200EE"))
                }

                if(orderType == "Delivery"){
                    binding.shopOrderDetailsAddressUserAddressTxt.text = address
                    binding.shopOrderDetailsAddressUserDistanceTxt.text = "( " + "%.2f KM".format(distance) + " )"
                    binding.shopOrderDetailsAddressCard.visibility = View.VISIBLE
                }
                else{
                    binding.shopOrderDetailsAddressCard.visibility = View.GONE
                }


               var currentOrderStatus = orderStatus
                var currentOrderType = orderType

                loadRecyclerView()
                checkOrder(currentOrderStatus,currentOrderType)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        dataRef.addValueEventListener(postListener)
    }

    private fun loadRecyclerView(){
        var orderKey = arguments?.getString("orderKey").toString()
        database = FirebaseDatabase.getInstance().getReference("Orders")
        var dataRef = database.child(orderKey).child("itemOrdered")

        var orderArrayList = ArrayList<OrderDetailsClass>()

        val postListener = object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                orderArrayList.clear()
                snapshot.children.forEach {
                    var itemImg = it.child("itemImage").value.toString()
                    var itemName = it.child("itemName").value.toString()
                    var itemPrice = it.child("itemPrice").value.toString()
                    var itemQty = it.child("itemQty").value.toString()
                    orderArrayList.add(OrderDetailsClass(itemImg,itemName,itemPrice,itemQty))
                }
//                Log.d("Orders",orderArrayList.toString())
                var recyclerView = binding.shopOrderDetailsRecyclerView
                recyclerView.layoutManager = LinearLayoutManager(context)

                recyclerView.adapter = OrderDetailsAdapter(orderArrayList){ OrderDetailsClass, position ->

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }

        dataRef.addListenerForSingleValueEvent(postListener)
    }

    private fun cancelOrder(){
        var orderKey = arguments?.getString("orderKey").toString()
        database = FirebaseDatabase.getInstance().getReference("Orders")
        var dataRef =  database.child(orderKey)

        dataRef.child("orderStatus").setValue("Cancel").addOnCompleteListener{
            if(it.isSuccessful){
                Toast.makeText(context,"Order Has Been Cancelled",Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(context,"Error in Database",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            Toast.makeText(context,it.toString() ,Toast.LENGTH_SHORT).show()
        }
    }

    private fun acceptOrder(){
        var orderKey = arguments?.getString("orderKey").toString()
        database = FirebaseDatabase.getInstance().getReference("Orders")
        var dataRef =  database.child(orderKey)

        dataRef.child("orderStatus").setValue("Preparing").addOnCompleteListener{
            if(it.isSuccessful){
                Toast.makeText(context,"Order Has Been Accepted",Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(context,"Error in Database",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            Toast.makeText(context,it.toString() ,Toast.LENGTH_SHORT).show()
        }
    }

    private fun completeOrder(){
        var orderKey = arguments?.getString("orderKey").toString()
        database = FirebaseDatabase.getInstance().getReference("Orders")
        var dataRef =  database.child(orderKey)

        dataRef.child("orderStatus").setValue("Completed").addOnCompleteListener{
            if(it.isSuccessful){
                Toast.makeText(context,"Order Has Been Completed",Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(context,"Error in Database",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            Toast.makeText(context,it.toString() ,Toast.LENGTH_SHORT).show()
        }
    }

    private fun unCancelOrder(){
        var orderKey = arguments?.getString("orderKey").toString()
        database = FirebaseDatabase.getInstance().getReference("Orders")
        var dataRef =  database.child(orderKey)

        dataRef.child("orderStatus").setValue("Pending").addOnCompleteListener{
            if(it.isSuccessful){
                Toast.makeText(context,"Order Has Been Un-Cancel",Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(context,"Error in Database",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            Toast.makeText(context,it.toString() ,Toast.LENGTH_SHORT).show()
        }
    }

    private fun deliverOrder(){
        var orderKey = arguments?.getString("orderKey").toString()
        database = FirebaseDatabase.getInstance().getReference("Orders")
        var dataRef =  database.child(orderKey)

        dataRef.child("orderStatus").setValue("Delivering").addOnCompleteListener{
            if(it.isSuccessful){
                Toast.makeText(context,"Order is Delivering",Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(context,"Error in Database",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            Toast.makeText(context,it.toString() ,Toast.LENGTH_SHORT).show()
        }
    }

    private fun readyForPickUp(){
        var orderKey = arguments?.getString("orderKey").toString()
        database = FirebaseDatabase.getInstance().getReference("Orders")
        var dataRef =  database.child(orderKey)

        dataRef.child("orderStatus").setValue("Pick Up Ready").addOnCompleteListener{
            if(it.isSuccessful){
                Toast.makeText(context,"Order is Ready for Pick Up",Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(context,"Error in Database",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener{
            Toast.makeText(context,it.toString() ,Toast.LENGTH_SHORT).show()
        }
    }
}