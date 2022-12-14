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
import androidx.navigation.fragment.findNavController
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentUserProfileBinding
import com.example.stationerygo.databinding.FragmentUserProfilePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern

private lateinit var binding: FragmentUserProfilePasswordBinding
private lateinit var auth: FirebaseAuth

class UserProfilePassword : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserProfilePasswordBinding.inflate(
            inflater,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()
        binding.userProfilePasswordUpdateBtn.setOnClickListener{
            validatePassword()
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun validatePassword(){

        var oldPass = binding.oldUpdatePasswordTextfield.editText?.text.toString()
        var pass1 = binding.newUpdatePassword1Textfield.editText?.text.toString()
        var pass2 = binding.newUpdatePassword2Textfield.editText?.text.toString()
        var errorChecker = false


//        var email = arguments?.getString("email").toString()
//        var loginUser = auth.signInWithEmailAndPassword(email,oldPass).isSuccessful
//        Log.d("User",email)
        if(oldPass.isEmpty() || oldPass == null){
            binding.oldUpdatePasswordTextfield.error = "Required*"
            errorChecker = true
        }
        else{
            binding.oldUpdatePasswordTextfield.isErrorEnabled = false
        }

        var PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=*])(?=\\S+$).{4,}$";
        var pattern_password = Pattern.compile(PASSWORD_PATTERN)
        if(pass1.isEmpty() || pass1 == null){
            binding.newUpdatePassword1Textfield.error = "Required*"
            errorChecker = true
        }
        else if(!pattern_password.matcher(pass1).matches()){
            binding.newUpdatePassword1Textfield.error = "Must contain 1 Capital, 1 Number, 1 Symbol !"
            errorChecker = true
        }
        else if(pass1.count() < 5){
            binding.newUpdatePassword1Textfield.error = "Password must have more then 5!"
            errorChecker = true
        }
        else if(pass1.count() > 20){
            binding.newUpdatePassword1Textfield.error= "Password must be less then 20!"
            errorChecker = true
        }
        else{
            binding.newUpdatePassword1Textfield.isErrorEnabled = false
        }

        if(pass2.isEmpty() || pass2 == null){
            binding.newUpdatePassword2Textfield.error = "Required*"
            errorChecker = true
        }
        else if(!pass1.equals(pass2)){
            binding.newUpdatePassword2Textfield.error = "Needs To Be Same As Above Password!"
            errorChecker = true
        }
        else{
            binding.newUpdatePassword2Textfield.isErrorEnabled = false
        }

        if(!errorChecker){
            val alartDialog = AlertDialog.Builder(context,R.style.AlertDialogCustom)
            alartDialog.apply {
                setTitle("Confirm Update Password?")
                setMessage("Are you sure you want to update password?")
                setPositiveButton("Confirm"){ _: DialogInterface?, _: Int ->
                    updateUserPassword(pass1)
                }
                setNegativeButton("Cancel"){_, _ ->
                }
            }.create().show()
        }
        else{
            Toast.makeText(context,"Please check above Input Boxes!",Toast.LENGTH_SHORT).show()
        }


    }

    private fun updateUserPassword(newPass:String){
        val progress = ProgressDialog(activity)
        progress.setTitle("Changing to New Password")
        progress.show()

        var userEmail = auth.currentUser?.email.toString()
        var oldPassword = binding.oldUpdatePasswordTextfield.editText?.text.toString()

        val user = auth.currentUser

        val credentials = EmailAuthProvider.getCredential(userEmail,oldPassword)

        user?.reauthenticate(credentials)?.addOnSuccessListener {

            binding.newUpdatePassword1Textfield.isErrorEnabled = false

            user!!.updatePassword(newPass).addOnSuccessListener{

                    Toast.makeText(context,"User Password Updated!",Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                    progress.dismiss()

            }.addOnFailureListener{
                Log.d("Password", it.toString())
                progress.dismiss()
            }
        }?.addOnFailureListener{
            binding.oldUpdatePasswordTextfield.error = "Invalid Password"
            progress.dismiss()
        }



    }

}