package com.example.stationerygo.StoreProducts

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stationerygo.R
import com.example.stationerygo.StoreCreate.CreateStoreData
import com.example.stationerygo.StoreProducts.CreateProducts.CreateProductData
import com.example.stationerygo.StoreProducts.CreateProducts.dataToFirebase
import com.example.stationerygo.databinding.FragmentProductListsBinding
import com.example.stationerygo.databinding.FragmentStoreListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

private lateinit var binding: FragmentProductListsBinding
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth

class ProductLists : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
            binding = FragmentProductListsBinding.inflate(
            inflater,
            container,
            false
        )
        getShopName()

        binding.createProductsBtn.setOnClickListener{
            findNavController().navigate(R.id.action_productLists_to_createProducts)
        }

        return binding.root
    }
    private fun getShopName(){
        auth = Firebase.auth
        var uid = auth.currentUser?.uid.toString()
        database = FirebaseDatabase.getInstance().getReference("Stores")
        var postRef = database.child(uid)
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var dataName: String = ""
                    dataName = snapshot.child("storeName").value.toString()

//                    dataName = it.value.toString()
                    Log.d("Details",dataName)

                if (dataName == ""){
                    Log.d("Details","Error")
                }
                else
                getDataFromFirebase(dataName)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("Details", "Error: " + error.toString())
            }
        }

        postRef.addValueEventListener(postListener)
    }
    private fun getDataFromFirebase(dataName:String){
        var productData = ArrayList<ProductListData>()
        database = FirebaseDatabase.getInstance().getReference("Products")
        val postReference = database.child(dataName)
        Log.d("Details","Data gotten:" + dataName)
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productData.clear()
                snapshot.children.forEach{
//                    Log.d("Products", it.child("productName").toString())
                    var productImage = it.child("productImage").value.toString()
                    var productName = it.child("productName").value.toString()
                    productData.add(ProductListData(productImage,productName))
                }

                val recyclerView = binding.recyclerviewProducts
                recyclerView.layoutManager = LinearLayoutManager(context)

                recyclerView.adapter = ProductListAdapter(productData){ ProductListData,position:Int ->
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Products",error.toString())
            }
        }
        postReference.addValueEventListener(postListener)
    }


}