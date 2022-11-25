package com.example.stationerygo.StorePage.StoreDetailPage.ItemDetailPage

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.stationerygo.Cart.Cart_Data
import com.example.stationerygo.R
import com.example.stationerygo.StoreProducts.CreateProducts.CreateProductData
import com.example.stationerygo.databinding.FragmentItemDetailPageBinding
import com.example.stationerygo.databinding.FragmentStoreDetailPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

private lateinit var binding: FragmentItemDetailPageBinding
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth
private var productImage: String = ""
private var productName: String = ""
private var productQty: String = ""
private var productPrice: String = ""
private var amountToCart: String = "1"


class ItemDetailPage : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentItemDetailPageBinding.inflate(
            inflater,
            container,
            false
        )
        auth = Firebase.auth

        getDetailFromDatabase()

        binding.addToCartBtn.setOnClickListener{
            checkCart()
        }

        binding.itemMinusQuantityBtn.setOnClickListener {
            minusAmount()
        }
        binding.itemAddQuantityBtn.setOnClickListener {
//            binding.itemCurrentQuantityTxt.text
              addAmount()
        }

        return binding.root
    }

    private fun checkIfExistInCart(){
        var uid = auth.currentUser?.uid.toString()
        var itemID = arguments?.getString("itemID").toString()
        var storeID = arguments?.getString("storeID").toString()

        database = FirebaseDatabase.getInstance().getReference("Cart")
        val dataRef = database.child(uid).child(storeID).child(itemID)

        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    var itemQty = snapshot.child("itemQty").getValue().toString()
                    binding.itemCurrentQuantityTxt.text = itemQty
                    binding.addToCartBtn.text = "Update Amount"

                    var newPrice = productPrice.toDouble() * itemQty.toInt()
                    binding.itemPriceTxt.text = "RM %.2f".format(newPrice)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        dataRef.addListenerForSingleValueEvent(postListener)
    }

    private fun getDetailFromDatabase(){
        var itemID = arguments?.getString("itemID").toString()
        var storeID = arguments?.getString("storeID").toString()
        database = FirebaseDatabase.getInstance().getReference("Products")
        var dataRef = database.child(storeID).child(itemID)
        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var itemName = snapshot.child("productName").getValue().toString()
                var itemImage = snapshot.child("productImage").getValue().toString()
                var itemQty = snapshot.child("productQty").getValue().toString()
                var itemPrice = snapshot.child("productPrice").getValue().toString()
                var desc = snapshot.child("productDescription").value.toString()
                productImage = itemImage
                productName = itemName
                productQty = itemQty
                productPrice = itemPrice

                binding.itemPrixeTxx.text = "RM " + productPrice + " / Unit"
                binding.itemDescTxt.text = desc

                binding.itemNameTxt.text = itemName
                Picasso.get()
                    .load(itemImage)
                    .into(binding.imageView2)

                binding.itemQTYTxt.text = "In Stock [" + itemQty + "]"

                binding.itemPriceTxt.text = "RM " + itemPrice

                if(itemQty == "0"){
                    binding.cardOfPaymentsCard.visibility = View.GONE
                }
                else{
                    binding.cardOfPaymentsCard.visibility = View.VISIBLE
                }

//                Log.d("Items",itemName)

                checkIfExistInCart()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        }
            dataRef.addValueEventListener(postListener)
    }

    private fun checkCart(){
        val alartDialog = AlertDialog.Builder(context,R.style.AlertDialogCustom)
        var uid = auth.currentUser?.uid.toString()
        var storeID = arguments?.getString("storeID").toString()

        database = FirebaseDatabase.getInstance().getReference("Cart")
        var dataRef = database.child(uid)

        var postRef = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value != null){
                    if(snapshot.child(storeID).exists()){
                        addToCart()
                    }
                    else{
                        alartDialog.apply {
                            setTitle("Clear Cart")
                            setMessage("Items from other shop are in the Cart. Do you want to clear them?")
                            setPositiveButton("Clear"){ _: DialogInterface?, _: Int ->
                                    clearCart()
                            }
                            setNegativeButton("Cancel"){_, _ ->
                                Toast.makeText(context,"Failed to Add Item to Cart",Toast.LENGTH_SHORT).show()
                            }
                        }.create().show()
                    }
                }
                else{
                    addToCart()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        dataRef.addListenerForSingleValueEvent(postRef)
    }




    private fun addToCart(){
        auth = FirebaseAuth.getInstance()
        var uid = auth.currentUser?.uid.toString()
        var storeID = arguments?.getString("storeID").toString()
        var itemID = arguments?.getString("itemID").toString()
        var itemCurrentAmount = productQty

        Log.d("Cart", itemCurrentAmount)

        database = FirebaseDatabase.getInstance().getReference("Cart")
        var dataref = database.child(uid).child(storeID).child(itemID)

        var postRef = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var cart = Cart_Data(itemID, productImage, productName,amountToCart, productPrice, itemCurrentAmount)
               if(snapshot.exists()){
                   dataref.child("itemQty").setValue(amountToCart).addOnCompleteListener{
                       Toast.makeText(context,"Item Amount Been Updated",Toast.LENGTH_SHORT).show()
                   }
                       .addOnFailureListener{
                       Toast.makeText(context,"$it",Toast.LENGTH_SHORT).show()
                   }
//                Toast.makeText(context,"Item Already in Cart",Toast.LENGTH_SHORT).show()
               }
                else{
                   dataref.setValue(cart)
                   Toast.makeText(context,"Item Added To Cart",Toast.LENGTH_SHORT).show()
               }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        }

        dataref.addListenerForSingleValueEvent(postRef)


//      Toast.makeText(context,"Remove Previous Cart",Toast.LENGTH_SHORT).show()

    }

    private fun clearCart(){
        auth = FirebaseAuth.getInstance()
        var uid = auth.currentUser?.uid.toString()


        database = FirebaseDatabase.getInstance().getReference("Cart")
        var dataref = database.child(uid)

        var postRef = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.ref.removeValue()
                Toast.makeText(context,"Cart has been cleared",Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        }
        dataref.addListenerForSingleValueEvent(postRef)
    }

    private fun minusAmount(){
        var currentAmount = binding.itemCurrentQuantityTxt.text.toString()
        var currentPrice = productPrice
        if( currentAmount.toInt() <= 1){
            Toast.makeText(context,"Cannot be less then 1",Toast.LENGTH_SHORT).show()
        }
        else{
            var newAmount = currentAmount.toInt() - 1
            binding.itemCurrentQuantityTxt.text = newAmount.toString()

            var newPrice = currentPrice.toDouble() * newAmount
            binding.itemPriceTxt.text = "RM %.2f".format(newPrice)

            amountToCart = newAmount.toString()
        }
    }

    private fun addAmount(){
        var currentAmount = binding.itemCurrentQuantityTxt.text.toString()
        var currentPrice = productPrice
        if( currentAmount.toInt() >= productQty.toInt()){
            Toast.makeText(context,"Already Max Amount",Toast.LENGTH_SHORT).show()
        }
        else{
            var newAmount = currentAmount.toInt() + 1
            binding.itemCurrentQuantityTxt.text = newAmount.toString()

            var newPrice = currentPrice.toDouble() * newAmount
            binding.itemPriceTxt.text = "RM %.2f".format(newPrice)

            amountToCart = newAmount.toString()
        }
    }
}