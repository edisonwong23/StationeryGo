package com.example.stationerygo.ProfilePage

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.example.stationerygo.R
import com.example.stationerygo.StoreCreate.CreateStoreData
import com.example.stationerygo.databinding.FragmentHomePageBinding
import com.example.stationerygo.databinding.FragmentUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
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
            val email = user.email

            binding.displayUserName.text = name
            Picasso.get()
                .load(user.photoUrl.toString())
                .into(binding.profileImageImg)

            binding.userProfileemaildisplayTxt.text = email
        }

        binding.userChangeLocationBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homePage_to_registerAddressPage)
        }

        binding.shopBtn.setOnClickListener{
//                checkUserOwnShop()
//            findNavController().navigate(R.id.action_homePage_to_createStorePage)
            findNavController().navigate(R.id.action_homePage_to_mainStorePage)
        }

        binding.editUserBtn.setOnClickListener{
            findNavController().navigate(R.id.action_homePage_to_userProfileEdit)
        }

        binding.changePasswordBtn.setOnClickListener{
            findNavController().navigate(R.id.action_homePage_to_userProfilePassword)
        }

        binding.logoutBtn.setOnClickListener{
            val alartDialog = AlertDialog.Builder(context,R.style.AlertDialogCustom)

            alartDialog.apply {
                setTitle("Logout User?")
                setMessage("Are you sure you want to logout?")
                setPositiveButton("Logout"){ _: DialogInterface?, _: Int ->
                    auth.signOut()
                    findNavController().navigate(R.id.action_homePage_to_loginPage)
                }
                setNegativeButton("Cancel"){_, _ ->
                }
            }.create().show()

        }

        return binding.root
    }


}