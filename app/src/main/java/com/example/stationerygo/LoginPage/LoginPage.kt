package com.example.stationerygo.LoginPage

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentLoginPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

private lateinit var binding: FragmentLoginPageBinding
//private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth

class LoginPage : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginPageBinding.inflate(
            inflater,
            container,
            false
        )

        val user = Firebase.auth.currentUser
        if(user != null){
            findNavController().navigate(R.id.action_loginPage_to_homePage)
        }

        binding.signUPTextview.setOnClickListener {
            findNavController().navigate(R.id.action_loginPage_to_registerPage)
        }
        binding.loginBtn.setOnClickListener { validChecker() }

        return binding.root
    }

    fun validChecker() {
        var username = binding.usernameTextfield.editText?.text.toString()
        var password = binding.passwordTextview.editText?.text.toString()
        var errorChecker = false

        if (username.isEmpty()) {
            binding.usernameTextfield.error = "Empty*"
            errorChecker = true
        }
        else{
            binding.usernameTextfield.isErrorEnabled = false
            errorChecker = false
        }

        if(password.isEmpty()){
            binding.passwordTextview.error = "Empty"
            errorChecker = true
        }
        else{
            binding.passwordTextview.isErrorEnabled = false
            errorChecker = false
        }

        if(!errorChecker)
            loginAuthUser()
        else
            Toast.makeText(getContext(),"Check User Login", Toast.LENGTH_SHORT).show()

    }

    fun loginAuthUser(){
        var email = binding.usernameTextfield.editText?.text.toString()
        var password = binding.passwordTextview.editText?.text.toString()
        auth = Firebase.auth

        val progress = ProgressDialog(activity)
        progress.setTitle("Logging In")
        progress.show()

        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(){
            task ->
            if(task.isSuccessful){
                progress.hide()
                findNavController().navigate(R.id.action_loginPage_to_homePage)
            }
            else{
                progress.hide()
                Toast.makeText(context,"Fail to Login",Toast.LENGTH_SHORT).show()
            }
        }
    }

//    fun loginUser(){
//
//        var username = binding.usernameTextfield.editText?.text.toString()
//        var password = binding.passwordTextview.editText?.text.toString()
//
//        database = FirebaseDatabase.getInstance().getReference("Users")
//        val checkUser = database.child(username)
//
//        val postListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                var users = dataSnapshot.getValue(DataUserLogin::class.java)
////                var user_username = dataSnapshot.child("username").getValue(String::class.java)
////                var user_password = dataSnapshot.child("password").getValue(String::class.java)
//
//                if(username == users?.username && password == users?.password){
////                    Toast.makeText(getContext(),"Login Successful", Toast.LENGTH_SHORT).show()
//                }
//                else
//                    Toast.makeText(getContext(),"User does not exist", Toast.LENGTH_SHORT).show()
//
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                Toast.makeText(getContext(), databaseError.toString(), Toast.LENGTH_SHORT).show()
//            }
//        }
//        checkUser.addValueEventListener(postListener)
//
//    }

}