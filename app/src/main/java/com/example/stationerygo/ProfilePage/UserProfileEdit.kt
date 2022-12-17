package com.example.stationerygo.ProfilePage

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
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
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import com.example.stationerygo.LoginPage.DataUserRegister
import com.example.stationerygo.LoginPage.imagePathFromFirebase
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentUserProfileBinding
import com.example.stationerygo.databinding.FragmentUserProfileEditBinding
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import java.util.*
import java.util.regex.Pattern

private lateinit var binding: FragmentUserProfileEditBinding
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth
private val pickImage = 100
private var imageUri: Uri? = null
lateinit var imageView: ImageView
private var dataToFirebase: Intent? = null
private var oldImagePath = ""
private var allUserPhone: MutableList<String> = ArrayList<String>()
private var oldPhone :String? = null

class UserProfileEdit : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentUserProfileEditBinding.inflate(
            inflater,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()
        var uid = auth.currentUser?.uid
        loadUserProfile(uid!!)

        checkPhoneNumber()

        imageView = binding.imageView3
       binding.imageView3.setOnClickListener{
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }

        binding.editProfileUpdateProfileBtn.setOnClickListener{
            validationCheck()
        }

        return binding.root
    }

    private fun loadUserProfile(uid:String){
        database = FirebaseDatabase.getInstance().getReference("Users")
        var dataRef = database.child(uid)

        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var userDisplayName = snapshot.child("displayUsername").value.toString()
                var phone = snapshot.child("phone").value.toString()
                var userImage = snapshot.child("userImage").value.toString()
                oldImagePath = snapshot.child("userImage").value.toString()
                oldPhone = phone

                binding.userProfileEditDisplayNameEditTextField.setText(userDisplayName)
                binding.userProfileEditPhoneNumberEditTextField.setText(phone)

                Picasso.get()
                    .load(userImage)
                    .into(binding.imageView3)

            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,"Error getting from database",Toast.LENGTH_SHORT).show()
            }

        }
        dataRef.addListenerForSingleValueEvent(postListener)
    }

    private fun validationCheck(){
        var displayUserName = binding.userProfileEditDisplayNameTextfield.editText?.text.toString()
        var phone = binding.userProfileEditPhoneNumberTextfield.editText?.text.toString()
        var phoneExist = false


        var errorChecker = false

        if(oldPhone != phone){
            for (data in allUserPhone){
                if(data == phone){
                    phoneExist = true
                }
            }
        }


        if(displayUserName.isEmpty()){
            binding.userProfileEditDisplayNameTextfield.error = "Required*"
            errorChecker = true
        }
        else if(!displayUserName.matches("^[a-zA-Z]*$".toRegex()))
        {
            binding.userProfileEditDisplayNameTextfield.error = "Must Only Contain Letters & No Space Allow!"
            errorChecker = true
        }
        else if(displayUserName.count() <= 1){
            binding.userProfileEditDisplayNameTextfield.error = " Must be Less then 1 Letters!"
            errorChecker = true
        }
        else if(displayUserName.count() > 20){
            binding.userProfileEditDisplayNameTextfield.error = "Display User Name cannot be more then 20"
            errorChecker = true
        }
        else{
            binding.userProfileEditDisplayNameTextfield.isErrorEnabled = false
        }

        var PHONE_PATTERN = "\\+?6?(?:01[0-46-9]\\d{7,8}|0\\d{8})"
        var pattern_phone = Pattern.compile(PHONE_PATTERN)
        if(phone.isEmpty()){
            binding.userProfileEditPhoneNumberTextfield.error = "Required*"
            errorChecker = true
        }
        else{
            if(phone.count() != 10){
               binding.userProfileEditPhoneNumberTextfield.error = "Invalid Phone Number"
                errorChecker = true
            }
            else if(!pattern_phone.matcher(phone).matches()){
                binding.userProfileEditPhoneNumberTextfield.error = "Invalid Malaysian Phone Number"
                errorChecker = true
            }
            else if(phoneExist){
                binding.userProfileEditPhoneNumberTextfield.error = "Phone Already Exist"
                errorChecker = true
            }
            else{
                binding.userProfileEditPhoneNumberTextfield.isErrorEnabled = false
            }
        }

        if(!errorChecker)
        {
            val alartDialog = AlertDialog.Builder(context,R.style.AlertDialogCustom)
            alartDialog.apply {
                setTitle("Confirm Update User?")
                setMessage("Are you sure you want to update profile?")
                setPositiveButton("Confirm"){ _: DialogInterface?, _: Int ->
                    if(dataToFirebase == null){
                        updateUserProfile(oldImagePath)
                    }
                    else{
                        uploadToFirebase(dataToFirebase)
                    }
                }
                setNegativeButton("Cancel"){_, _ ->
                }
            }.create().show()
        }
        else
            Toast.makeText(context,"Please Check Abobe Input Box",Toast.LENGTH_SHORT).show()
    }

    private fun updateUserProfile(imagePath:String){
        val progress = ProgressDialog(activity)
        progress.setTitle("Updating Account")
        progress.show()
        var uid = auth.currentUser?.uid
        var email = auth.currentUser?.email.toString()
        var userImgUri = imagePath
        database = FirebaseDatabase.getInstance().getReference("Users")
        var dataRef = database.child(uid.toString())

        if(imagePath.isEmpty() || imagePath == null){
            userImgUri = oldImagePath
        }
        else{
            userImgUri = imagePath
        }

        var displayUserName = binding.userProfileEditDisplayNameTextfield.editText?.text.toString()
        var phone = binding.userProfileEditPhoneNumberTextfield.editText?.text.toString()

//        var userData = DataUserRegister(displayUserName,email,phone, userImgUri)

        val profileUpdate = userProfileChangeRequest {
            displayName = displayUserName
            photoUri = userImgUri.toUri()
        }

        auth.currentUser?.updateProfile(profileUpdate)?.addOnCompleteListener{
            if(it.isSuccessful){
                dataRef.child("displayUsername").setValue(displayUserName).addOnCompleteListener{
                    if(it.isSuccessful){
                        dataRef.child("phone").setValue(phone).addOnCompleteListener {
                            if(it.isSuccessful){
                                dataRef.child("userImage").setValue(userImgUri).addOnCompleteListener {
                                    Toast.makeText(context,"User Profile Update Successful!",Toast.LENGTH_SHORT).show()
                                    findNavController().navigateUp()
                                    progress.dismiss()
                                }
                            }
                        }
                    }
                    else{
                        Toast.makeText(context,"Error in Database",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else{
                progress.dismiss()
                Toast.makeText(context,"Error Updating User",Toast.LENGTH_SHORT).show()
            }
        }?.addOnFailureListener{
            Toast.makeText(context,"$it",Toast.LENGTH_SHORT).show()
        }


    }

    private fun checkPhoneNumber(){
        database = FirebaseDatabase.getInstance().getReference("Users")

        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    allUserPhone.add(it.child("phone").value.toString())
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        database.addValueEventListener(postListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            imageView.setImageURI(imageUri)
            dataToFirebase = data
            Log.d("Store", imageUri.toString())
        }
    }

    private  fun  uploadToFirebase (data: Intent?){
        val progress = ProgressDialog(activity)
        progress.setTitle("Uploading Image")
        progress.show()
        if(data != null){
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val refStorage = FirebaseStorage.getInstance().reference.child("images/$fileName")
            data?.data?.let {
                refStorage.putFile(it).addOnSuccessListener(
                    OnSuccessListener<UploadTask.TaskSnapshot>{
                            taskSnapshot -> taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                        imagePathFromFirebase = it.toString()
                        updateUserProfile(imagePathFromFirebase)
                        progress.dismiss()
                    }
                    })
            }
        }
    }

}