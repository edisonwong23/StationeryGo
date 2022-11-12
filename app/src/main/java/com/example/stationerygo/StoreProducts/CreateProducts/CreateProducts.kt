package com.example.stationerygo.StoreProducts.CreateProducts

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.navigation.fragment.findNavController
import com.example.stationerygo.R
import com.example.stationerygo.StoreProducts.EditProducts.oldImagePath
import com.example.stationerygo.databinding.FragmentCreateProductsBinding
import com.example.stationerygo.databinding.FragmentProductListsBinding
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import java.text.NumberFormat
import java.util.*

private lateinit var binding : FragmentCreateProductsBinding
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth
private val pickImage = 100
private var imageUri: Uri? = null
private lateinit var imageView: ImageView
var dataToFirebase: Intent? = null
private var current: String = "$0.00"

class CreateProducts : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateProductsBinding.inflate(
            inflater,
            container,
            false
        )

        changeKeyBoard()

        val stationeryBind = binding.productTypeEdittextField
        val stationery = resources.getStringArray(R.array.Stationery)
        val adapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_1,stationery)
        stationeryBind.setAdapter(adapter)

        imageView = binding.productCreateImageView

        binding.productCreateImageView.setOnClickListener{
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }

        binding.createProductBtn.setOnClickListener{
            validationCheck()
        }

        return binding.root
    }

    private fun changeKeyBoard(){
        binding.productPriceEdittextField.setRawInputType(
            Configuration.KEYBOARD_12KEY);
        binding.productPriceEdittextField.addTextChangedListener(object :
            TextWatcher {
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
        Log.d("Product", "$productPrice")

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

        if(productType == null){
            binding.productTypeTextField.error = "Required"
            errorChecker = true
        }
        else{
            if(productType.isEmpty()){
                binding.productTypeTextField.error = "Required"
                errorChecker = true
            }
            else{
                binding.productTypeTextField.isErrorEnabled = false
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
        }else if(newProductPrice < 1.00 && newProductPrice != 0.00){
            binding.productPriceTextField.error = "Price Needs To Be Over RM1.00"
            errorChecker = true
        }
        else if(newProductPrice == 0.00){
            binding.productPriceTextField.error = "Required*"
            errorChecker = true
        }
        else{
            binding.productPriceTextField.isErrorEnabled = false
        }
    }

        if(dataToFirebase == null){
            binding.productImageNotificationTxt.text = "Must Upload A Product Image"
            binding.productImageNotificationTxt.setTextColor(resources.getColor(R.color.errorTextColor))
            errorChecker = true
        }
        else{
            binding.productImageNotificationTxt.text = ""
        }

    if(errorChecker){
        Toast.makeText(context,"Please Check All Input Boxes",Toast.LENGTH_SHORT).show()
    }
    else{
        if(dataToFirebase == null){
            Toast.makeText(context,"Please Check All Input Boxes",Toast.LENGTH_SHORT).show()
        }
        else{

             getShopName(productName,productDesc,productType,productQty,productPrice)
        }

    }

}

    private  fun  uploadImageToFirebase (data: Intent?, dataName: String,name:String,desc:String,type:String,qty:String,price:String){
        if(data != null){
            val progress = ProgressDialog(activity)
            progress.setTitle("Uploading Product Details")
            progress.show()
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val refStorage = FirebaseStorage.getInstance().reference.child("products/$fileName")
            data?.data?.let {
                refStorage.putFile(it).addOnSuccessListener(
                    OnSuccessListener<UploadTask.TaskSnapshot>{
                            taskSnapshot -> taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                        var imagePathFromFirebase = it.toString()
                        addItemToFirebase(imagePathFromFirebase,dataName,name,desc,type,qty,price)
                        progress.hide()
                    }
                    }).addOnFailureListener({
                        Toast.makeText(context,it.toString(),Toast.LENGTH_SHORT).show()
                })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            imageView.setImageURI(imageUri)
            dataToFirebase = data
        }
    }


    private fun getShopName(name:String,desc:String,type:String,qty:String,price:String){
        auth = Firebase.auth
        var uid = auth.currentUser?.uid.toString()
        database = FirebaseDatabase.getInstance().getReference("Stores")
        var postRef = database.child(uid)
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                    var dataName: String = ""
                    dataName = snapshot.child("storeID").value.toString()
                if(dataName == ""){
                    Toast.makeText(context,"Error in Database",Toast.LENGTH_SHORT).show()
                }
                else
                uploadImageToFirebase(dataToFirebase,dataName,name,desc,type,qty,price)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Details", "Error: " + error.toString())
            }
        }

        postRef.addValueEventListener(postListener)
    }

    private fun addItemToFirebase(imageURI: String,storeName:String,name:String,desc:String,type:String,qty:String,price:String){

             database = FirebaseDatabase.getInstance().getReference("Products")
             var product = CreateProductData(imageURI,name,desc,type,qty,price)
             database.child(storeName).push().setValue(product).addOnCompleteListener {
                 findNavController().navigateUp()
            Toast.makeText(getContext(), "Product Created", Toast.LENGTH_SHORT).show()
//                 findNavController().navigate(R.id.action_createProducts_to_productLists)
        }.addOnFailureListener{
            Log.d("Store", "Failure happen" +it.toString())
            Toast.makeText(getContext(), it.toString(), Toast.LENGTH_SHORT).show()
        }
    }

}