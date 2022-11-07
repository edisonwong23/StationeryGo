package com.example.stationerygo.Cart

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentCartPageBinding
import com.example.stationerygo.databinding.FragmentStoreDetailPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

private lateinit var binding : FragmentCartPageBinding
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth

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

        return binding.root
    }


    private fun loadRecyclerCart(){
        var uid = auth.currentUser?.uid.toString()
        var storeID = arguments?.getString("StoreID").toString()
        var cartArray = ArrayList<Cart_Data>()

        database = FirebaseDatabase.getInstance().getReference("Cart")
        var dataRef = database.child(uid).child(storeID)
        Log.d("Cart",storeID)
        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                cartArray.clear()
                snapshot.children.forEach{
                    var productID = it.child("itemID").value.toString()
                    var productImage = it.child("itemImage").value.toString()
                    var productName = it.child("itemName").value.toString()
                    var productQty = it.child("itemQty").value.toString()
                    cartArray.add(Cart_Data(productID,productImage,productName,productQty))
                }
                Log.d("Cart",cartArray.toString())

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
}
