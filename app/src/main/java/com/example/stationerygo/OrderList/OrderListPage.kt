package com.example.stationerygo.OrderList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentOrderListBinding
import com.example.stationerygo.databinding.FragmentUserProfileBinding

private lateinit var binding : FragmentOrderListBinding

class OrderListPage : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderListBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }


}