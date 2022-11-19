package com.example.stationerygo.StorePage.StoreDetailPage

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stationerygo.R
import com.example.stationerygo.StorePage.StoreListData
import com.example.stationerygo.StoreProducts.ProductListAdapter
import com.example.stationerygo.StoreProducts.ProductListData
import com.example.stationerygo.databinding.FragmentStoreDetailPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso


private lateinit var binding: FragmentStoreDetailPageBinding
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth
private var dataID = ""
private var dataName = ""

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
        auth = Firebase.auth

            getStoreDetails()



        binding.storeDetailsBtn.setOnClickListener {
            var storeName = arguments?.getString("storename").toString()
            var bundle = bundleOf(
                "storeName" to storeName
            )

            findNavController().navigate(R.id.action_storeDetailPage_to_storeDetailsDetailsPage,bundle)
        }

        binding.clearProductTypeSearchBtn.setOnClickListener {
            spinnerAdapter()
        }

        binding.navigateToCartFAB.setOnClickListener{
            var bundle = bundleOf(
                "StoreID" to dataID,
                "StoreName" to dataName
            )
//            Log.d("Payment","ID: $dataID - Name: $dataName")
            findNavController().navigate(R.id.action_storeDetailPage_to_cart_Page,bundle)
        }

        return binding.root
    }

    private fun spinnerAdapter(){
        val spinner: Spinner = binding.productTypeSpinner
        val product = resources.getStringArray(R.array.Stationery2)
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.Stationery2,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                var productType = product[p2]

                if(productType == "None"){
                    getStoreProducts(dataID)
                }
                else{
                    getSearchedProducts(productType)
                }


//                Log.d("Payment","Selected Payment is $selectedPaymentType")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun getSearchedProducts(productType:String){
        val progress = ProgressDialog(activity)
        progress.setTitle("Loading Products")
        progress.show()

        var productData = ArrayList<StoreProductData>()
        database = FirebaseDatabase.getInstance().getReference("Products")
        val postReference = database.child(dataID)
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productData.clear()
                var i = 0
                snapshot.children.forEach{
                    var loadType = it.child("productType").value.toString()
                    if(loadType ==  productType){
                        var productImage = it.child("productImage").value.toString()
                        var productID = it.key
                        var productName = it.child("productName").value.toString()
                        var productQty = it.child("productQty").value.toString()
                        var productPrice = it.child("productPrice").value.toString()
                        productData.add(StoreProductData(i++,productID,productImage,productName,productQty,productPrice))
                    }
                }
                if(productData.isEmpty()){
                    progress.hide()
                    binding.storeDetailsRecyclerView.visibility = View.GONE
                    binding.noProductFoundText.visibility = View.VISIBLE
                }else{
                    progress.hide()
                    binding.noProductFoundText.visibility = View.GONE
                    binding.storeDetailsRecyclerView.visibility = View.VISIBLE
                    val recyclerView = binding.storeDetailsRecyclerView
                    recyclerView.layoutManager = LinearLayoutManager(context)
                    recyclerView.adapter = StoreProductAdapter(productData){ StoreProductData, position:Int ->
                        var productID = productData[position].productID
                        var bundle = bundleOf(
                            "storeID" to dataID,
                            "itemID" to productID,
                        )
                        findNavController().navigate(R.id.action_storeDetailPage_to_itemDetailPage,bundle)
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

    private fun getStoreDetails(){
        val progress = ProgressDialog(activity)
        progress.setTitle("Loading Store")
        progress.show()
        var storeStatus = arguments?.getString("storeStatus").toString()
        var storeName = arguments?.getString("storename").toString()
        database = FirebaseDatabase.getInstance().getReference("Stores")
        var postRef = database.orderByChild("storeName").equalTo(storeName)
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
//                Log.d("Details", snapshot.value.toString())
                var dataImg = ""
                snapshot.children.forEach{
                    dataID = it.child("storeID").value.toString()
                    dataName = it.child("storeName").value.toString()
                    dataImg = it.child("storeImage").value.toString()
                    Picasso.get()
                        .load(dataImg)
                        .into(binding.storeimageImg)
                }
                if(storeStatus == "Close"){
                    progress.hide()
                    binding.productTypeSpinner.visibility = View.INVISIBLE
                    binding.clearProductTypeSearchBtn.visibility = View.INVISIBLE
                    binding.imageView4.visibility = View.VISIBLE
                }
                else if(dataName == ""){
                    progress.hide()
                    Toast.makeText(context,"No Item Listed!",Toast.LENGTH_SHORT).show()
                }
                else{
//                    (activity as AppCompatActivity).supportActionBar?.title = dataName
//                    getStoreProducts(dataID)
                    spinnerAdapter()
                    progress.hide()
                    checkTotalInCart()
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
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productData.clear()
                var i = 0
                snapshot.children.forEach{
                    var productImage = it.child("productImage").value.toString()
                    var productID = it.key
                    var productName = it.child("productName").value.toString()
                    var productQty = it.child("productQty").value.toString()
                    var productPrice = it.child("productPrice").value.toString()
                    productData.add(StoreProductData(i++,productID,productImage,productName,productQty,productPrice))
                }
                if(productData.isEmpty()){
                    progress.hide()
                    binding.noProductFoundText.visibility = View.VISIBLE
                    binding.storeDetailsRecyclerView.visibility = View.GONE
//                    Toast.makeText(context,"Store is Empty",Toast.LENGTH_SHORT).show()
                }else{
                    progress.hide()
                    binding.noProductFoundText.visibility = View.GONE
                    binding.storeDetailsRecyclerView.visibility = View.VISIBLE
                    val recyclerView = binding.storeDetailsRecyclerView
                    recyclerView.layoutManager = LinearLayoutManager(context)
                    recyclerView.adapter = StoreProductAdapter(productData){ StoreProductData, position:Int ->
                        var productID = productData[position].productID
                        var bundle = bundleOf(
                            "storeID" to dataID,
                            "itemID" to productID,
                        )
                        findNavController().navigate(R.id.action_storeDetailPage_to_itemDetailPage,bundle)
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

    private fun checkTotalInCart(){
        val progress = ProgressDialog(activity)
        progress.show()
        auth = FirebaseAuth.getInstance()
        var uid = auth.currentUser?.uid.toString()

        database = FirebaseDatabase.getInstance().getReference("Cart")
        var dataRef = database.child(uid).child(dataID)
        var postRef = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null){
                    var total = snapshot.childrenCount
                    binding.totalInCartTxt.visibility = View.VISIBLE
                    binding.totalInCartTxt.text = total.toString()
                    progress.hide()
                }
                else{
                    binding.navigateToCartFAB.visibility = View.GONE
                    binding.totalInCartTxt.visibility = View.INVISIBLE
                    progress.hide()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        }
        dataRef.addListenerForSingleValueEvent(postRef)

    }
}