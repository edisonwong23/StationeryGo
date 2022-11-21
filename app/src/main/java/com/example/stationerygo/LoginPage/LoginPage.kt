package com.example.stationerygo.LoginPage

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentLoginPageBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

private lateinit var binding: FragmentLoginPageBinding
//private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth
private lateinit var fusedLocationClient: FusedLocationProviderClient

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        checkRequestForLocationPermission()

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

    private fun checkRequestForLocationPermission(){
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            AlertDialog.Builder(context)
                .setTitle("Location Permission Needed")
                .setMessage("This app needs the Location permission, please accept to use location functionality")
                .setPositiveButton(
                    "OK"
                ) { _, _ ->
                    //Prompt the user once explanation has been shown
                    requestLocationPermission()
                }
                .create()
                .show()

        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            MY_PERMISSIONS_REQUEST_LOCATION
        )
    }

    private fun validChecker() {
        var username = binding.usernameTextfield.editText?.text.toString()
        var password = binding.passwordTextview.editText?.text.toString()
        var errorChecker = false

        if (username.isEmpty()) {
            binding.usernameTextfield.error = "Required*"
            errorChecker = true
        }
        else{
            binding.usernameTextfield.isErrorEnabled = false
        }

        if(password.isEmpty()){
            binding.passwordTextview.error = "Required*"
            errorChecker = true
        }
        else{
            binding.passwordTextview.isErrorEnabled = false
        }

        if(!errorChecker)
            loginAuthUser()
        else
            Toast.makeText(getContext(),"Invalid User!", Toast.LENGTH_SHORT).show()

    }

    private fun loginAuthUser(){
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

    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
//        private const val MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 66
    }

}