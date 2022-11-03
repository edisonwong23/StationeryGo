package com.example.stationerygo.ProfilePage

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
            binding.displayUserName.text = name
            Picasso.get()
                .load(user.photoUrl.toString())
                .into(binding.profileImageImg)
        }

        binding.shopBtn.setOnClickListener{
                checkUserOwnShop()
//            findNavController().navigate(R.id.action_homePage_to_createStorePage)
//            findNavController().navigate(R.id.action_homePage_to_mainStorePage)
        }

        binding.logoutBtn.setOnClickListener{
            auth.signOut()
            findNavController().navigate(R.id.action_homePage_to_loginPage)
        }

        return binding.root
    }

    private fun checkUserOwnShop() {
        val progress = ProgressDialog(activity)
        progress.setTitle("Checking Store")
        progress.show()

        auth = Firebase.auth
        val user = Firebase.auth.currentUser?.uid
        Log.d("User",user.toString())

        database = FirebaseDatabase.getInstance().getReference("Stores")
        val checkUser = database.child(user.toString())

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var users = dataSnapshot.getValue(CreateStoreData::class.java)
                Log.d("User","Current Owner:" + users.toString())
                if(users == null){
                    findNavController().navigate(R.id.action_homePage_to_createStorePage)
                    progress.hide()
                }
                else{
                    findNavController().navigate(R.id.action_homePage_to_mainStorePage)
                    progress.hide()
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(getContext(), databaseError.toString(), Toast.LENGTH_SHORT).show()
                Log.d("User","Fail to Get User")
                progress.hide()
            }
        }
        checkUser.addValueEventListener(postListener)
    }

}