package com.example.stationerygo.StoreOwner

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.stationerygo.R
import com.example.stationerygo.StoreCreate.CreateStoreData
import com.example.stationerygo.databinding.FragmentCreateStorePageBinding
import com.example.stationerygo.databinding.FragmentMainStorePageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

private lateinit var binding : FragmentMainStorePageBinding
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth


class MainStorePage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainStorePageBinding.inflate(
            inflater,
            container,
            false
        )

        auth = Firebase.auth
        val getUser = auth.currentUser
        getUser.let {
            var email = getUser?.email.toString()
            var uid = getUser?.uid.toString()
            loginUser(uid)
            Log.d("Stores","User UID: " +uid)
        }

        return binding.root
    }


    private fun loginUser(uid:String){
        val progress = ProgressDialog(activity)
        progress.setTitle("Loading Store")
        progress.show()

        database = FirebaseDatabase.getInstance().getReference("Stores")
        val checkUser = database.child(uid)

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var users = dataSnapshot.getValue(CreateStoreData::class.java)
                Log.d("Store","Current Owner:" + users.toString())
                var storename = dataSnapshot.child("storeName").getValue(String::class.java)
//                var user_password = dataSnapshot.child("password").getValue(String::class.java)
                (activity as AppCompatActivity).supportActionBar?.title = storename
                progress.hide()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(getContext(), databaseError.toString(), Toast.LENGTH_SHORT).show()
            }
        }
        checkUser.addValueEventListener(postListener)

    }

}