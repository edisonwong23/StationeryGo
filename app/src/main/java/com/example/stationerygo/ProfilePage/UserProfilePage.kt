package com.example.stationerygo.ProfilePage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentHomePageBinding
import com.example.stationerygo.databinding.FragmentUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

private lateinit var binding : FragmentUserProfileBinding
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth

class UserProfilePage : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserProfileBinding.inflate(
            inflater,
            container,
            false
        )
        auth = Firebase.auth
        val user = Firebase.auth.currentUser
        user?.let {
            val name = user.displayName
            binding.displayUserName.text = name
            Picasso.get()
                .load(user.photoUrl.toString())
                .into(binding.profileImageImg)
        }

        binding.shopBtn.setOnClickListener{
//            findNavController().navigate(R.id.action_homePage_to_createStorePage)
            findNavController().navigate(R.id.action_homePage_to_mainStorePage)
        }

        binding.logoutBtn.setOnClickListener{
            auth.signOut()
            findNavController().navigate(R.id.action_homePage_to_loginPage)
        }

        return binding.root
    }

}