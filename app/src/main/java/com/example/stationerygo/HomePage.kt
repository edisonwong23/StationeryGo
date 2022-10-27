package com.example.stationerygo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.ui.AppBarConfiguration
import com.example.stationerygo.OrderList.OrderListPage
import com.example.stationerygo.ProfilePage.UserProfilePage
import com.example.stationerygo.StorePage.StoreList
import com.example.stationerygo.databinding.FragmentHomePageBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

private lateinit var binding: FragmentHomePageBinding
private lateinit var bottomNav : BottomNavigationView

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




        return binding.root
    }

}