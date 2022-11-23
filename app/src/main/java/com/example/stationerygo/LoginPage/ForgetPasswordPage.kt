package com.example.stationerygo.LoginPage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentForgetPasswordPageBinding
import com.example.stationerygo.databinding.FragmentLoginPageBinding
import com.google.firebase.auth.FirebaseAuth

private lateinit var binding: FragmentForgetPasswordPageBinding
private lateinit var auth: FirebaseAuth

class ForgetPasswordPage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForgetPasswordPageBinding.inflate(
            inflater,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()

        binding.sendEmailBtn.setOnClickListener{
            sendEmail()
        }

        binding.goBackBtn.setOnClickListener{
            findNavController().navigateUp()
        }

        return binding.root
    }

    private fun sendEmail(){

        var userEmail = binding.forgetPasswordTxt.editText?.text.toString()

        auth.sendPasswordResetEmail(userEmail).addOnCompleteListener {
            if(it.isSuccessful){
                binding.beforeResetPasswordTxt.visibility = View.GONE
                binding.forgetPasswordTxt.visibility = View.GONE
                binding.sendEmailBtn.visibility = View.GONE

                binding.imageView9.visibility = View.VISIBLE
                binding.goBackBtn.visibility = View.VISIBLE
                binding.emailSendTxt.visibility = View.VISIBLE
            }

            else{
                Toast.makeText(context,"Fail to Send Email",Toast.LENGTH_SHORT).show()
            }
        }

    }

}