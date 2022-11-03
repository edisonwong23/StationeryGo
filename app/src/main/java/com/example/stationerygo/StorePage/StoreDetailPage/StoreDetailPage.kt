package com.example.stationerygo.StorePage.StoreDetailPage

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stationerygo.R
import com.example.stationerygo.StorePage.StoreListData
import com.example.stationerygo.StoreProducts.ProductListAdapter
import com.example.stationerygo.StoreProducts.ProductListData
import com.example.stationerygo.databinding.FragmentStoreDetailPageBinding
import com.google.firebase.database.*
import com.squareup.picasso.Picasso


private lateinit var binding: FragmentStoreDetailPageBinding
private lateinit var database: DatabaseReference


class StoreDetailPage : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentStoreDetailPageBinding.inflate(
            inflater,
            container,
            false
        )
        getStoreDetails()

        return binding.root
    }

    private fun getStoreDetails(){
        val progress = ProgressDialog(activity)
        progress.setTitle("Loading Store")
        progress.show()
        var storeName = arguments?.getString("storename").toString()
        database = FirebaseDatabase.getInstance().getReference("Stores")
        var postRef = database.orderByChild("storeName").equalTo(storeName)
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
//                Log.d("Details", snapshot.value.toString())
                var dataName = ""
                var dataImg = ""
                snapshot.children.forEach{
                    dataName = it.child("storeName").value.toString()
                    dataImg = it.child("storeImage").value.toString()
                    Picasso.get()
                        .load(dataImg)
                        .into(binding.storeimageImg)
                }
                if(dataName == ""){
                    progress.hide()
                    Toast.makeText(context,"Couldn't get Info from Database",Toast.LENGTH_SHORT).show()
                }
                else{
                    (activity as AppCompatActivity).supportActionBar?.title = dataName
                    getStoreProducts(dataName)
                    progress.hide()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                progress.hide()
                Log.d("Details", "Error: " + error.toString())
            }
        }

        postRef.addValueEventListener(postListener)
    }

    private fun getStoreProducts(dataName:String){
        val progress = ProgressDialog(activity)
        progress.setTitle("Loading Products")
        progress.show()

        var productData = ArrayList<StoreProductData>()
        database = FirebaseDatabase.getInstance().getReference("Products")
        val postReference = database.child(dataName)
        Log.d("Details","Data gotten:" + dataName)
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productData.clear()
                var i = 0
                snapshot.children.forEach{
//                    Log.d("Products", it.child("productName").toString())
                    var productImage = it.child("productImage").value.toString()
                    var productName = it.child("productName").value.toString()
                    var productQty = it.child("productQty").value.toString()
                    productData.add(StoreProductData(i++,productImage,productName,productQty))
                }
                Log.d("Products",productData.toString())
                if(productData.isEmpty()){
                    progress.hide()
                    Toast.makeText(context,"Couldn't get Info from Database",Toast.LENGTH_SHORT).show()
                }else{
                    progress.hide()
                    val recyclerView = binding.storeDetailsRecyclerView
                    recyclerView.layoutManager = LinearLayoutManager(context)
                    Log.d("Products","Loaded into Here First")
                    recyclerView.adapter = StoreProductAdapter(productData){ StoreProductData, position:Int ->
                        var productName = productData[position].productName
                        Toast.makeText(context,productName,Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progress.hide()
                Log.d("Products",error.toString())
            }
        }
        postReference.addValueEventListener(postListener)
    }
}