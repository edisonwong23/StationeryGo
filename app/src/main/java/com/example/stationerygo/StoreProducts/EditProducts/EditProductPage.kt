package com.example.stationerygo.StoreProducts.EditProducts

import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.stationerygo.R
import com.example.stationerygo.StoreProducts.CreateProducts.CreateProductData
import com.example.stationerygo.databinding.FragmentCreateProductsBinding
import com.example.stationerygo.databinding.FragmentEditProductPageBinding
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.text.NumberFormat

private lateinit var binding: FragmentEditProductPageBinding
private lateinit var database: DatabaseReference
private var current: String = ""

class EditProductPage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditProductPageBinding.inflate(
            inflater,
            container,
            false
        )

        binding.productPriceEdittextField.setRawInputType(Configuration.KEYBOARD_12KEY);
        binding.productPriceEdittextField.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }


            private var discount_amount_edit_text = binding.productPriceEdittextField
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (s.toString() != current) {
                    discount_amount_edit_text.removeTextChangedListener(this)

                    val cleanString: String = s.replace("""[$,.]""".toRegex(), "")

                    val parsed = cleanString.toDouble()
                    val formatted = NumberFormat.getCurrencyInstance().format((parsed / 100))

                    current = formatted
                    discount_amount_edit_text.setText(formatted)
                    discount_amount_edit_text.setSelection(formatted.length)

                    discount_amount_edit_text.addTextChangedListener(this)
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })



        loadProduct()

        val stationeryBind = binding.productTypeEdittextField
        val stationery = resources.getStringArray(R.array.Stationery)
        val adapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_1,stationery)
        stationeryBind.setAdapter(adapter)

        binding.editProductBtn.setOnClickListener{
            validationCheck()
        }

        return binding.root
    }

    private fun loadProduct(){
       var storeID = arguments?.getString("storeID").toString()
        var productKey = arguments?.getString("productKey").toString()
//        Log.d("Product", "StoreID: $storeID // ProductKey: $productKey")
        database = FirebaseDatabase.getInstance().getReference("Products")
        var productRef = database.child(storeID).child(productKey)

        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var productImage = snapshot.child("productImage").value.toString()
                var productName = snapshot.child("productName").value.toString()
                var productDesc = snapshot.child("productDescription").value.toString()
                var productType = snapshot.child("productType").value.toString()
                var productQty = snapshot.child("productQty").value.toString()
                var productPrice = snapshot.child("productPrice").value.toString()

                Picasso.get()
                    .load(productImage)
                    .into(binding.productEditImageView)

                binding.productNameEdittextField.setText(productName)
                binding.productDescriptionEdittextField.setText(productDesc)
                binding.productTypeEdittextField.setText(productType,false)
                binding.productQtyEdittextField.setText(productQty)
                binding.productPriceEdittextField.setText(productPrice)

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        productRef.addListenerForSingleValueEvent(postListener)
    }

    private fun validationCheck(){
       var productName = binding.productNameTextField.editText?.text.toString()
       var productDesc =  binding.productDescriptionTextField.editText?.text.toString()
       var productType =  binding.productTypeEdittextField.text.toString()
       var productQty =  binding.productQtyTextField.editText?.text.toString()
       var productPrice =  current.replace("$","").trim()
        var newProductPrice = 0.00
        if(productPrice.contains(",")){
            newProductPrice = productPrice.replace(",","").trim().toDouble()
        }
        else{
            newProductPrice = productPrice.toDouble()
        }
        Log.d("Product", "$newProductPrice")

        var errorChecker = false

        if(productName == null || productName.isEmpty()){
            binding.productNameTextField.error = "Required*"
            errorChecker = true
        }
        else{
            if(productName.count() > 20){
                binding.productNameTextField.error = "Cannot over 20 words"
                errorChecker = true
            }else{
                binding.productNameTextField.isErrorEnabled = false
            }
        }

        if(productDesc == null || productDesc.isEmpty()){
            binding.productDescriptionTextField.error = "Required*"
            errorChecker = true
        }else{
            if(productDesc.count() > 50){
                binding.productDescriptionTextField.error = "Cannot over 50 words"
                errorChecker = true
            }else{
                binding.productDescriptionTextField.isErrorEnabled = false
            }
        }

        if(productQty == null || productQty.isEmpty()){
            binding.productQtyTextField.error = "Required*"
            errorChecker = true
        }else{
            if(productQty.toInt() > 1000){
                binding.productQtyTextField.error = "Cannot over 1000 quantity"
                errorChecker = true
            }
            else{
                binding.productQtyTextField.isErrorEnabled = false
            }
        }

        if(newProductPrice == null){
            binding.productPriceTextField.error = "Required*"
            errorChecker = true
        }
        else{
            if(newProductPrice > 1000.00 ){
                binding.productPriceTextField.error = "Price Cannot Be over RM1000.00"
                errorChecker = true
            }else if(newProductPrice < 1.00){
                binding.productPriceTextField.error = "Price Needs To Be Over RM1.00"
                errorChecker = true
            }
            else{
                binding.productPriceTextField.isErrorEnabled = false
            }
        }

        if(errorChecker){
            Toast.makeText(context,"Please Check All Input Boxes",Toast.LENGTH_SHORT).show()
        }
        else{
            updateProduct(productName,productDesc,productType,productQty,newProductPrice.toString())
        }

    }


    private fun updateProduct(name:String,desc:String,type:String,quan:String,price:String){
        var storeID = arguments?.getString("storeID").toString()
        var productKey = arguments?.getString("productKey").toString()
        var prodImg = "https://firebasestorage.googleapis.com/v0/b/stationerygo-18271.appspot.com/o/products%2F916cf288-db5d-44bb-b0ca-d36cebaf2861.jpg?alt=media&token=ff3f6a1d-94e4-4b0e-b3d0-4523580fdcf5"

        var product = CreateProductData(prodImg,name,desc,type,quan,price)

        database = FirebaseDatabase.getInstance().getReference("Products")
        database.child(storeID).child(productKey).setValue(product).addOnCompleteListener{
            findNavController().navigateUp()
        }.addOnFailureListener{
            Log.d("Product",it.toString())
            Toast.makeText(context,"Error Detected",Toast.LENGTH_SHORT).show()
        }

//        Toast.makeText(context,"No Error Detected",Toast.LENGTH_SHORT).show()

    }



}