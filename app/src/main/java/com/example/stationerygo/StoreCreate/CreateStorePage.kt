package com.example.stationerygo.StoreCreate

import android.app.Activity.RESULT_OK
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentCreateStorePageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

private lateinit var binding : FragmentCreateStorePageBinding
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth
private val pickImage = 100
private var imageUri: Uri? = null
lateinit var imageView: ImageView
private var startingTime = ""
private var endingTime = ""
private var checkStartingTime : Date? = null
private var checkEndingTime : Date? = null
private var allStoreNames: MutableList<String> = ArrayList<String>()

class CreateStorePage : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateStorePageBinding.inflate(
            inflater,
            container,
            false
        )

//        auth = Firebase.auth
//        val user = Firebase.auth.currentUser
//        user?.let {
//            userEmail = user.email.toString()
//        }

        checkStoreName()

        imageView = binding.storeImage
        binding.storeImage.setOnClickListener{
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }

        binding.proceedBtn.setOnClickListener{
            validationCheck();
        }

        getStartTime(binding.operatingTimeEdittextField,requireContext())

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            imageView.setImageURI(imageUri)
            Log.d("Store",imageUri.toString())
        }
    }

    private fun validationCheck(){

        var storeName = binding.storeNameTextField.editText?.text.toString()
        var description = binding.storeDescriptionTextField.editText?.text.toString()
        var timeStart = startingTime
        var timeEnd =   endingTime
        var operatingDays = "Sunday,Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,"
        var email = binding.storeEmailTextField.editText?.text.toString()
        var phone = binding.storePhoneTextField.editText?.text.toString()
        var errorChecker = false
        var storeNameExist = false

        for(data in allStoreNames){
            if(data == storeName){
                storeNameExist = true
            }
        }


        if(imageUri == null){
            binding.createStoretext.text = "Must Upload Store Image"
            binding.createStoretext.setTextColor(resources.getColor(R.color.errorTextColor))
            errorChecker = true
        }
        else{
            binding.createStoretext.visibility = View.INVISIBLE
        }

        if(storeName.isEmpty()){
            binding.storeNameTextField.error = "Required*"
            errorChecker = true
        }
        else if(storeName.count() > 20){
            binding.storeNameTextField.error = "Cannot more then 20 words"
            errorChecker = true
        }
        else if(storeNameExist == true){
            binding.storeNameTextField.error = "Store Name Already Existed"
            errorChecker = true
        }
        else{
            binding.storeNameTextField.isErrorEnabled = false
        }

        if(description.isEmpty()){
            binding.storeDescriptionTextField.error = "Required*"
            errorChecker = true
        }
        else if(description.count() > 150){
            binding.storeDescriptionTextField.error = "Cannot more then 50 words"
            errorChecker = true
        }
        else{
            binding.storeDescriptionTextField.isErrorEnabled = false
        }

        if(startingTime.isEmpty() || endingTime.isEmpty()){
            binding.operatingTimeTextField.error = "Invalid Operating Time"
            errorChecker = true
        }
        else if(endingTime < startingTime){
            binding.operatingTimeTextField.error = "Please Choose a Valid Operating Time"
            errorChecker = true
        }
        else{
            binding.operatingTimeTextField.isErrorEnabled = false
        }

               if(!binding.sundayCheckBox.isChecked){
                   operatingDays = operatingDays.replace("Sunday,","")
               }
               if(!binding.mondayCheckBox.isChecked){
                   operatingDays =  operatingDays.replace("Monday,","")

               }
               if(!binding.tuesdayCheckBox.isChecked){
                   operatingDays =  operatingDays.replace("Tuesday,","")

               }
               if(!binding.wednesdayCheckBox.isChecked){
                   operatingDays = operatingDays.replace("Wednesday,","")

               }
               if(!binding.thursdayCheckBox.isChecked){
                   operatingDays = operatingDays.replace("Thursday,","")
               }
               if(!binding.fridayCheckBox.isChecked){
                   operatingDays = operatingDays.replace("Friday,","")
               }
               if(!binding.saturdayCheckBox.isChecked){
                   operatingDays = operatingDays.replace("Saturday,","")
               }


        if(operatingDays.isEmpty() || operatingDays == " "){
            binding.operatingDaysTxt.text = "Must Select 1 Operating Days*"
            binding.operatingDaysTxt.setTextColor(resources.getColor(R.color.errorTextColor))
            errorChecker = true
        }
        else{
            binding.operatingDaysTxt.visibility = View.INVISIBLE
        }

            Log.d("Store",operatingDays)

        var PHONE_PATTERN = "\\+?6?(?:01[0-46-9]\\d{7,8}|0\\d{8})"
        var pattern_phone = Pattern.compile(PHONE_PATTERN)
        if(phone.isEmpty()){
            binding.storePhoneTextField.error = "Required*"
            errorChecker = true
        }
        else if(!pattern_phone.matcher(phone).matches()){
            binding.storePhoneTextField.error = "Invalid Phone Number"
            errorChecker = true
        }
        else{
            binding.storePhoneTextField.isErrorEnabled = false
        }

        if(email.isEmpty()){
            binding.storeEmailTextField.error = "Required"
            errorChecker = true
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.storeEmailTextField.error = "Invalid Email Address"
            errorChecker = true
        }
        else{
            binding.storeEmailTextField.isErrorEnabled = false
        }

       if(errorChecker){

           Toast.makeText(context,"Please Check All Above Input Boxes!",Toast.LENGTH_SHORT).show()

       }else{
           val bundle = bundleOf(
               "storeImage" to imageUri.toString(),
               "storeName" to storeName.toString(),
               "description" to description.toString(),
               "timeStart" to timeStart.toString(),
               "timeEnd" to timeEnd.toString(),
               "operatingDays" to operatingDays,
               "email" to email.toString(),
               "phone" to phone.toString(),
           )

           findNavController().navigate(R.id.action_createStorePage_to_createStoreAddressPage,bundle)
       }
    }

    private fun getStartTime(textView: TextView, context: Context){

        val cal = Calendar.getInstance()

        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)

            checkStartingTime = cal.time

            textView.text = SimpleDateFormat("HH:mm").format(cal.time)
            startingTime = SimpleDateFormat("HH:mm").format(cal.time)
            getEndTime(textView,requireContext())
        }

        textView.setOnClickListener {
            TimePickerDialog(context, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

    }

    private fun getEndTime(textView: TextView, context: Context){

        val cal = Calendar.getInstance()

        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)

            checkEndingTime = cal.time

            endingTime = SimpleDateFormat("HH:mm").format(cal.time)
            textView.append(" - " + SimpleDateFormat("HH:mm").format(cal.time))
        }

        TimePickerDialog(context, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    private fun checkStoreName(){
        database = FirebaseDatabase.getInstance().getReference("Stores")

        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    allStoreNames.add(it.child("storeName").value.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        database.addValueEventListener(postListener)
    }

}