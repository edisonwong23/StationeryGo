package com.example.stationerygo.Cart

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
import com.example.stationerygo.databinding.FragmentCartPageBinding
import com.example.stationerygo.databinding.FragmentStoreDetailPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.DecimalFormat

private lateinit var binding : FragmentCartPageBinding
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth
private var totalPaymentAmount: String = ""
private var itemInCart = false
private var deliveryFee = "0.00"
private var userCurrentAddress = ""

class Cart_Page : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
             binding = FragmentCartPageBinding.inflate(
            inflater,
            container,
            false
        )
        auth = FirebaseAuth.getInstance()
        loadRecyclerCart()

        getUserAddress()

        binding.pickupBtn.setOnClickListener {
            binding.pickupBtn.setBackgroundResource(R.drawable.not_transparent_button)
            binding.deliveryBtn.setBackgroundResource(R.drawable.transparent_button)
            deliveryFee = "0.00"
            binding.deliveryFeeAmountTxt.text = deliveryFee
            binding.addressEditCard.visibility = View.GONE
            loadRecyclerCart()
        }

        binding.deliveryBtn.setOnClickListener {
            binding.pickupBtn.setBackgroundResource(R.drawable.transparent_button)
            binding.deliveryBtn.setBackgroundResource(R.drawable.not_transparent_button)
            deliveryFee = "5.00"
            binding.deliveryFeeAmountTxt.text = deliveryFee
            binding.addressEditCard.visibility = View.VISIBLE
            loadRecyclerCart()
        }

        binding.proceedPaymentBtn.setOnClickListener{

            if(itemInCart){
                proceedToPayment()
            }
           else
               Toast.makeText(context,"Cart Is Empty,Please Add Item to Cart!",Toast.LENGTH_SHORT).show()
        }
        return binding.root
    }


    private fun getUserAddress(){
        var uid = auth.currentUser?.uid.toString()
        database = FirebaseDatabase.getInstance().getReference("Users")
        val userRef = database.child(uid)

        val postListner = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var userAddress = snapshot.child("address").value.toString()

                binding.cartCurrentAddressTxt.text = userAddress
                userCurrentAddress = userAddress
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        userRef.addListenerForSingleValueEvent(postListner)
    }

    private fun loadRecyclerCart(){
        var uid = auth.currentUser?.uid.toString()
        var storeID = arguments?.getString("StoreID").toString()
        var cartArray = ArrayList<Cart_Data>()

        database = FirebaseDatabase.getInstance().getReference("Cart")
        var dataRef = database.child(uid).child(storeID)
        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                itemInCart = false
                cartArray.clear()
                var productQtyTotal = 0
                var productPriceTotal = 0.00
                snapshot.children.forEach{
                    var productID = it.child("itemID").value.toString()
                    var productImage = it.child("itemImage").value.toString()
                    var productName = it.child("itemName").value.toString()
                    var productQty = it.child("itemQty").value.toString()
                    var productPrice = it.child("itemPrice").value.toString()
                    var itemCurrentAmount = it.child("itemCurrentAmount").value.toString()
                    var currentPrice = productPrice.toDouble() * productQty.toInt()

                    productQtyTotal += productQty.toInt()

                    productPriceTotal += currentPrice
                    itemInCart = true
                    cartArray.add(Cart_Data(productID,productImage,productName,productQty,productPrice,itemCurrentAmount,storeID,uid))
                }
//                  Log.d("Cart", "Total Qty: $productQtyTotal")
//                  Log.d("Cart", "Total Price: $productPriceTotal")

                  var subTotal = productPriceTotal
                  binding.subTotalAmountTxt.text = "%.2f".format(subTotal)

                 binding.deliveryFeeAmountTxt.text = deliveryFee

                 var tax = productPriceTotal * 0.06
                 binding.taxAmountTxt.text = "%.2f".format(tax)

                 var totalAmount = subTotal+ deliveryFee.toDouble() + tax
                 binding.totalAmountAmountTxt.text = "%.2f".format(totalAmount)
                 totalPaymentAmount = "%.2f".format(totalAmount)
//                Log.d("Cart",cartArray.toString())

                val recycleView = binding.cartRecyclerview
                recycleView.layoutManager = LinearLayoutManager(context)

                recycleView.adapter = CartAdapet(cartArray){Cart_Data,position:Int ->

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Cart",error.toString())
            }

        }
        dataRef.addValueEventListener(postListener)
    }

    private fun proceedToPayment(){
        var storeName = arguments?.getString("StoreName").toString()
        var storeID = arguments?.getString("StoreID").toString()
        var bundle = bundleOf(
            "totalAmount" to totalPaymentAmount,
            "StoreName" to storeName,
            "orderType" to "Delivery",
            "storeID" to storeID,
            "userCurrentAddress" to userCurrentAddress,
        )
        findNavController().navigate(R.id.action_cart_Page_to_paymentPage,bundle)
    }
}
