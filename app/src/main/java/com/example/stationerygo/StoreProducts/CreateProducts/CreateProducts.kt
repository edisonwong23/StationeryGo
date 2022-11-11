package com.example.stationerygo.StoreProducts.CreateProducts

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
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
import java.util.*

private lateinit var binding : FragmentCreateProductsBinding
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth
private val pickImage = 100
private var imageUri: Uri? = null
private lateinit var imageView: ImageView
var dataToFirebase: Intent? = null

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

        imageView = binding.productCreateImageView

        binding.productCreateImageView.setOnClickListener{
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }

        binding.createProductBtn.setOnClickListener{
            getShopName()
        }

        return binding.root
    }

    private  fun  uploadImageToFirebase (data: Intent?, dataName: String){
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
                        addItemToFirebase(imagePathFromFirebase,dataName)
                        progress.hide()
                    }
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


    private fun getShopName(){
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
                uploadImageToFirebase(dataToFirebase,dataName)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Details", "Error: " + error.toString())
            }
        }

        postRef.addValueEventListener(postListener)
    }

    private fun addItemToFirebase(imageURI: String,storeName:String){

             database = FirebaseDatabase.getInstance().getReference("Products")
             var product = CreateProductData(imageURI,"Trash","Trash","Paper","12","50.00")
             database.child(storeName).push().setValue(product).addOnCompleteListener {
            Toast.makeText(getContext(), "Product Created", Toast.LENGTH_SHORT).show()
//                 findNavController().navigate(R.id.action_createProducts_to_productLists)
        }.addOnFailureListener{
            Log.d("Store", "Failure happen" +it.toString())
            Toast.makeText(getContext(), it.toString(), Toast.LENGTH_SHORT).show()
        }
    }

}