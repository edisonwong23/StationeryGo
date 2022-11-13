package com.example.stationerygo.LoginPage

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentLoginPageBinding
import com.example.stationerygo.databinding.FragmentRegisterPageBinding
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.internal.wait
import java.util.*
import java.util.regex.Pattern

private lateinit var binding: FragmentRegisterPageBinding
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth
private val pickImage = 100
private var imageUri: Uri? = null
lateinit var imageView: ImageView
var imagePathFromFirebase : String = ""
var dataToFirebase: Intent? = null

class RegisterPage : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterPageBinding.inflate(
            inflater,
            container,
            false
        )
        imageView = binding.imageView
        binding.imageUploadBtn.setOnClickListener{
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery,pickImage)
        }

        binding.registerBtn.setOnClickListener{validateUser()}
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            imageView.setImageURI(imageUri)
            dataToFirebase = data
        }
    }

    private  fun  uploadImageToFirebase (data: Intent?,email:String, password:String,displayNameUser:String){
        if(data != null){
            val progress = ProgressDialog(activity)
            progress.setTitle("Uploading Profile Details")
            progress.show()
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val refStorage = FirebaseStorage.getInstance().reference.child("images/$fileName")
            data?.data?.let {
                refStorage.putFile(it).addOnSuccessListener(
                    OnSuccessListener<UploadTask.TaskSnapshot>{
                            taskSnapshot -> taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                        imagePathFromFirebase = it.toString()
                        createAuthUser(email,password,displayNameUser)
                        progress.hide()
                    }
                    })
            }
        }
    }



    private fun createAuthUser(email:String, password:String,displayNameUser:String){
        val progress = ProgressDialog(activity)
        progress.setTitle("Setting Up Account")
        progress.show()
        auth.signInWithEmailAndPassword(email,password)
        val user = auth.currentUser
        val profileUpdate = userProfileChangeRequest {
            displayName = displayNameUser
            photoUri = imagePathFromFirebase.toUri()
        }
        savingContactNumber(user!!.uid,imagePathFromFirebase)
        user!!.updateProfile(profileUpdate)
            .addOnCompleteListener{
            task-> if(task.isSuccessful){
                progress.hide()
                findNavController().navigate(R.id.action_registerPage_to_homePage)
            }
                else
                Toast.makeText(context,"User Fail to Create",Toast.LENGTH_SHORT).show()
        }
    }

    private fun savingContactNumber(uid:String,userImage:String){
        var displayNameUser = binding.displayUsernameTextfield.editText?.text.toString()
        var email = binding.emailTextfield.editText?.text.toString()
        var phone = binding.phoneNumberTextfield.editText?.text.toString()
        database = FirebaseDatabase.getInstance().getReference("Users")
        var userImg = imagePathFromFirebase
        var userDetails = DataUserRegister(displayNameUser,email,phone,userImage)

        val dataRef = database.child(uid).setValue(userDetails)

        dataRef.addOnCompleteListener{
            if(it.isSuccessful){
                Toast.makeText(context,"User Contact Saved",Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(context,"User Fail to Create",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateUser(){
        var displayNameUser = binding.displayUsernameTextfield.editText?.text.toString()
        var email = binding.emailTextfield.editText?.text.toString()
        var password = binding.passwordTextfield.editText?.text.toString()
        var password2 = binding.passwordTextfield2.editText?.text.toString()
        var phone = binding.phoneNumberTextfield.editText?.text.toString()
        var errorChecker = false

        if(displayNameUser.isEmpty()){
            binding.displayUsernameTextfield.error = "Required*"
            errorChecker = true
        }
        else if(!displayNameUser.matches("^[a-zA-Z]*$".toRegex()))
        {
            binding.displayUsernameTextfield.error = "Must Only Contain Letters!"
            errorChecker = true
        }
        else if(displayNameUser.count() < 5){
            binding.displayUsernameTextfield.error = " Must be Less then 20 Letters!"
            errorChecker = true
        }
        else if(displayNameUser.count() > 20){
            binding.displayUsernameTextfield.error = "Display User Name is too short!"
            errorChecker = true
        }
        else{
            binding.displayUsernameTextfield.isErrorEnabled = false
        }

        if(email.isEmpty()){
            binding.emailTextfield.error = "Required*"
            errorChecker = true
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailTextfield.error = "Invalid Email Address"
            errorChecker = true
        }
        else{
            binding.emailTextfield.isErrorEnabled = false
        }

        var PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=*])(?=\\S+$).{4,}$";
        var pattern_password = Pattern.compile(PASSWORD_PATTERN)
        if(password.isEmpty()){
            binding.passwordTextfield.error = "Required*"
            errorChecker = true
        }
        else if(password.count() < 5){
            binding.passwordTextfield.error = "Password is too short!"
            errorChecker = true
        }
        else if(password.count() > 20){
            binding.passwordTextfield.error = "Password must be less then 20"
            errorChecker = true
        }
        else if(!pattern_password.matcher(password).matches()){
            binding.passwordTextfield.error = "Invalid Password Type"
            errorChecker = true
        }
        else
            binding.passwordTextfield.isErrorEnabled = false

        if(password2.isEmpty()){
            binding.passwordTextfield2.error = "Required*"
            errorChecker = true
        }
        else if(!password.equals(password2)){
            binding.passwordTextfield2.error = "Need to match above password!"
            errorChecker = true
        }
        else
            binding.passwordTextfield2.isErrorEnabled = false

        var PHONE_PATTERN = "\\+?6?(?:01[0-46-9]\\d{7,8}|0\\d{8})"
        var pattern_phone = Pattern.compile(PHONE_PATTERN)
        if(phone.isEmpty()){
            binding.phoneNumberTextfield.error = "Required*"
            errorChecker = true
        }
        else{
            if(phone.count() != 10){
                binding.phoneNumberTextfield.error = "Invalid Phone Number"
                errorChecker = true
            }
            else if(!pattern_phone.matcher(phone).matches()){
                binding.phoneNumberTextfield.error = "Invalid Malaysian Phone Number"
                errorChecker = true
            }
            else{
                binding.phoneNumberTextfield.isErrorEnabled = false
            }
        }

        if(dataToFirebase == null){
            binding.registerImageErrorTxt.visibility = View.VISIBLE
            errorChecker = true
        }
        else{
            binding.registerImageErrorTxt.visibility = View.GONE
        }

        if(!errorChecker)
            registerAuthUser()
        else
            Toast.makeText(context,"Please Check Abobe Input Box",Toast.LENGTH_SHORT).show()

    }

    private fun registerAuthUser(){
        val progress = ProgressDialog(activity)
        progress.setTitle("Registering Account")
        progress.show()

        var displayNameUser = binding.displayUsernameTextfield.editText?.text.toString()
        var email = binding.emailTextfield.editText?.text.toString()
        var password = binding.passwordTextfield.editText?.text.toString()


        auth = Firebase.auth
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(){task->
            if(task.isSuccessful){
                    progress.hide()
                    uploadImageToFirebase(dataToFirebase ,email,password,displayNameUser)
                    Log.d("Users",imagePathFromFirebase)
                }
            else{
            progress.hide()
            binding.emailTextfield.error = "Email Already Exist!"
        }
            }

        }
    }


