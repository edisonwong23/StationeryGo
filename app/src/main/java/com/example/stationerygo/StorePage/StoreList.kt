package com.example.stationerygo.StorePage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentStoreListBinding

private lateinit var binding: FragmentStoreListBinding

class StoreList : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStoreListBinding.inflate(
            inflater,
            container,
            false
        )



        return binding.root
    }

}