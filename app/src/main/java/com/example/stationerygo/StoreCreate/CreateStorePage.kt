package com.example.stationerygo.StoreCreate

import android.app.Activity.RESULT_OK
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentCreateStorePageBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

private lateinit var binding : FragmentCreateStorePageBinding
private lateinit var auth: FirebaseAuth
private val pickImage = 100
private var imageUri: Uri? = null
lateinit var imageView: ImageView
private var startingTime = ""
private var endingTime = ""

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

        val openingDays = binding.operatingDayStartEdittextField
        val closingDays = binding.operatingDayEndEdittextField
        val days = resources.getStringArray(R.array.Days)
        val adapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_1,days)
        openingDays.setAdapter(adapter)
        closingDays.setAdapter(adapter)

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

        var storeName = binding.storeNameTextField.editText?.text
        var description = binding.storeDescriptionTextField.editText?.text
        var timeStart = startingTime
        var timeEnd =   endingTime
        var dayStart = binding.operatingDayStartTextField.editText?.text
        var dayEnd = binding.operatingDayEndTextField.editText?.text
        var email = binding.storeEmailTextField.editText?.text
        var phone = binding.storePhoneTextField.editText?.text

        val bundle = bundleOf(
            "storeImage" to imageUri.toString(),
            "storeName" to storeName.toString(),
            "description" to description.toString(),
            "timeStart" to timeStart.toString(),
            "timeEnd" to timeEnd.toString(),
            "dayStart" to dayStart.toString(),
            "dayEnd" to dayEnd.toString(),
            "email" to email.toString(),
            "phone" to phone.toString(),
        )

        findNavController().navigate(R.id.action_createStorePage_to_createStoreAddressPage,bundle)
    }

    private fun getStartTime(textView: TextView, context: Context){

        val cal = Calendar.getInstance()

        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)

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
            endingTime = SimpleDateFormat("HH:mm").format(cal.time)
            textView.append(" - " + SimpleDateFormat("HH:mm").format(cal.time))
        }

        TimePickerDialog(context, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

}