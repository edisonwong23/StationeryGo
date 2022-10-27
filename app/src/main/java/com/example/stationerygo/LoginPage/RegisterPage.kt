package com.example.stationerygo.LoginPage

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentLoginPageBinding
import com.example.stationerygo.databinding.FragmentRegisterPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

private lateinit var binding: FragmentRegisterPageBinding
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth


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

        binding.registerBtn.setOnClickListener{validateUser()}
        // Inflate the layout for this fragment
        return binding.root
    }

    fun validateUser(){
        var email = binding.emailTextfield.editText?.text.toString()
        var password = binding.passwordTextfield.editText?.text.toString()
        var password2 = binding.passwordTextfield2.editText?.text.toString()
        var errorChecker = false

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

        if(password.isEmpty()){
            binding.passwordTextfield.error = "Required*"
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


        if(!errorChecker)
            registerAuthUser()
        else
            Toast.makeText(context,"Please Check Abobe Input Box",Toast.LENGTH_SHORT).show()

    }

    fun registerAuthUser(){
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
                auth.signInWithEmailAndPassword(email,password)
                val user = auth.currentUser

                val profileUpdate = userProfileChangeRequest {
                    displayName = displayNameUser
                }
                user!!.updateProfile(profileUpdate)
                findNavController().navigate(R.id.action_registerPage_to_homePage)
//                Toast.makeText(context,"Üser Successfully Created",Toast.LENGTH_SHORT).show()
                }
            else{
            progress.hide()
            Toast.makeText(context,"User Fail to Create",Toast.LENGTH_SHORT).show()
        }
            }

        }
    }

//    fun registerUser(){
//        database = FirebaseDatabase.getInstance().getReference("Users")
//        var display_Username = binding.displayusernameTextfield.editText?.text.toString()
//        var username = binding.usernameTextfield.editText?.text.toString()
//        var password = binding.passwordTextfield.editText?.text.toString()
//        var password2 = binding.passwordTextfield2.editText?.text.toString()
//        var email = binding.emailTextfield.editText?.text.toString()
//        var phone = binding.phoneTextfield.editText?.text.toString()
//
//        val user = DataUserRegister(display_Username,username,password,email,phone)
//
//        database.child(username).setValue(user).addOnSuccessListener {
//            Toast.makeText(getContext(), "User Created", Toast.LENGTH_SHORT).show()
//        }.addOnFailureListener{
//            Toast.makeText(getContext(), it.toString(), Toast.LENGTH_SHORT).show()
//        }
//    }

