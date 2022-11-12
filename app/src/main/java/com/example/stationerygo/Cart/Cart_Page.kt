package com.example.stationerygo.Cart

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        binding.proceedPaymentBtn.setOnClickListener{

            proceedToPayment()
        }
        return binding.root
    }


    private fun loadRecyclerCart(){
        var uid = auth.currentUser?.uid.toString()
        var storeID = arguments?.getString("StoreID").toString()
        var cartArray = ArrayList<Cart_Data>()

        database = FirebaseDatabase.getInstance().getReference("Cart")
        var dataRef = database.child(uid).child(storeID)
        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                cartArray.clear()
                var productQtyTotal = 0
                var productPriceTotal = 0.00
                snapshot.children.forEach{
                    var productID = it.child("itemID").value.toString()
                    var productImage = it.child("itemImage").value.toString()
                    var productName = it.child("itemName").value.toString()
                    var productQty = it.child("itemQty").value.toString()
                    var productPrice = it.child("itemPrice").value.toString()
                    var currentPrice = productPrice.toDouble() * productQty.toInt()

                    productQtyTotal += productQty.toInt()

                    productPriceTotal += currentPrice

                    cartArray.add(Cart_Data(productID,productImage,productName,productQty))
                }
//                  Log.d("Cart", "Total Qty: $productQtyTotal")
//                  Log.d("Cart", "Total Price: $productPriceTotal")

                  var subTotal = productPriceTotal
                  binding.subTotalAmountTxt.text = "%.2f".format(subTotal)

                 var deliveryFee = "5.00"
                 binding.deliveryFeeAmountTxt.text = deliveryFee

                 var tax = productPriceTotal * 0.06
                 binding.taxAmountTxt.text = "%.2f".format(tax)

                 var totalAmount = subTotal+ deliveryFee.toDouble() + tax.toDouble()
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
        dataRef.addListenerForSingleValueEvent(postListener)
    }

    private fun proceedToPayment(){
        var storeName = arguments?.getString("StoreName").toString()
        var storeID = arguments?.getString("StoreID").toString()
        var bundle = bundleOf(
            "totalAmount" to totalPaymentAmount,
            "StoreName" to storeName,
            "orderType" to "Delivery",
            "storeID" to storeID,
        )
        findNavController().navigate(R.id.action_cart_Page_to_paymentPage,bundle)
    }
}
