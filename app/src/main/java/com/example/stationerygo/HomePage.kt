package com.example.stationerygo

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.example.stationerygo.OrderList.OrderListPage
import com.example.stationerygo.ProfilePage.UserProfilePage
import com.example.stationerygo.StoreCreate.CreateStoreAddressPage
import com.example.stationerygo.StorePage.StoreList
import com.example.stationerygo.databinding.FragmentHomePageBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

private lateinit var binding: FragmentHomePageBinding
private lateinit var bottomNav : BottomNavigationView
private lateinit var auth : FirebaseAuth
private lateinit var database : DatabaseReference

class HomePage : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomePageBinding.inflate(
            inflater,
            container,
            false
        )

        auth = Firebase.auth

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

                    requestLocationPermission()
                }
                .create()
                .show()

        }




        val transaction: FragmentTransaction = activity?.getSupportFragmentManager()!!.beginTransaction()
        transaction.replace(R.id.fragment_Holder,StoreList())
        transaction.addToBackStack(null)
        transaction.commit()

        bottomNav = binding.bottomNavigationView
        bottomNav.setOnItemSelectedListener{
            when (it.itemId) {
                R.id.home -> {
                    val transaction: FragmentTransaction = activity?.getSupportFragmentManager()!!.beginTransaction()
                    transaction.replace(R.id.fragment_Holder, StoreList())
                    transaction.addToBackStack(null)
                    transaction.commit()
                    true
                }
                R.id.list -> {
                    val transaction: FragmentTransaction = activity?.getSupportFragmentManager()!!.beginTransaction()
                    transaction.replace(R.id.fragment_Holder, OrderListPage())
                    transaction.addToBackStack(null)
                    transaction.commit()
                    true
                }
                R.id.person -> {
                    val transaction: FragmentTransaction = activity?.getSupportFragmentManager()!!.beginTransaction()
                    transaction.replace(R.id.fragment_Holder, UserProfilePage())
                    transaction.addToBackStack(null)
                    transaction.commit()
                    true
                }
                else -> false
            }

        }



        checkUserAddress()

        return binding.root
    }

    private fun checkUserAddress(){
        var uid = auth.currentUser?.uid.toString()
        database = FirebaseDatabase.getInstance().getReference("Users")
        val checkUser = database.child(uid)

        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child("address").value.toString() == "None")
                {
                    findNavController().navigate(R.id.action_homePage_to_registerAddressPage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        checkUser.addValueEventListener(postListener)
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

    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
//        private const val MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 66
    }

}