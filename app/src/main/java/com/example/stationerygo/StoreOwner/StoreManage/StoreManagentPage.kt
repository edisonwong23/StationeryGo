package com.example.stationerygo.StoreOwner.StoreManage

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.stationerygo.R
import com.example.stationerygo.StoreCreate.*
import com.example.stationerygo.databinding.FragmentMainStorePageBinding
import com.example.stationerygo.databinding.FragmentStoreManagentPageBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.coroutines.async
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

private lateinit var binding: FragmentStoreManagentPageBinding
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth
private lateinit var fusedLocationClient: FusedLocationProviderClient
private var startingTime = ""
private var endingTime = ""
private val pickImage = 100
private var imageUri: Uri? = null
lateinit var imageView: ImageView
private var allStoreNames: MutableList<String> = ArrayList<String>()
private var oldStoreName: String ?= null
private var oldDesc: String ?= null
private var oldOperatingDays: String ?= null
private var oldEmail: String ?= null
private var oldPhone: String ?= null
private var dataToFirebase: Intent? = null
private var oldImagePath: String ?= null
private var imagePathFromFirebase: String ?= null
private var oldaddress: String ?= null
private var oldState: String ?= null
private var oldpostal: String ?= null
private var storeID: String ?= null

class StoreManagentPage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStoreManagentPageBinding.inflate(
            inflater,
            container,
            false
        )

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        auth = FirebaseAuth.getInstance()

        val stationeryBind = binding.stateEdittextField
        var stationery = resources.getStringArray(R.array.States)
        stationery = stationery.sortedArray()
        val adapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_1,stationery)
        stationeryBind.setAdapter(adapter)

        checkStoreName()
        loadStoreDetailsDetails()

        binding.updateStoreDetailsDetailsBtn.setOnClickListener {
            binding.updateStoreDetailsAddressCard.visibility = View.GONE
            binding.updateStoreDetailsDetailsCard.visibility = View.VISIBLE
        }

        binding.updateStoreDetailsAddressBtn.setOnClickListener{
            binding.updateStoreDetailsAddressCard.visibility = View.VISIBLE
            binding.updateStoreDetailsDetailsCard.visibility = View.GONE
        }

        binding.updateStoreDetailsToFireBaseBtn.setOnClickListener {
            validationCheck()
        }

        binding.createStoreBtn.setOnClickListener {
            addressValidation()
        }

        binding.autoFillAddressBtn.setOnClickListener {
            Log.d("Store","Location Button Pressed")
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                AlertDialog.Builder(context)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        //Prompt the user once explanation has been shown
                        requestLocationPermission()
                    }
                    .create()
                    .show()

            }
            fusedLocationClient.getCurrentLocation(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                object : CancellationToken() {
                    override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                        CancellationTokenSource().token

                    override fun isCancellationRequested() = false
                }
            )
                .addOnSuccessListener { location: Location? ->
                    Log.d("Store","Location Starting")
                    val progress = ProgressDialog(activity)
                    progress.setTitle("Getting Current Location")
                    val lat = location?.latitude
                    val lon = location?.longitude
                    Log.d("Store","Lat: $lat")
                    Log.d("Store","Lon: $lon")
                    progress.show()
                    if (location == null)
                    {
                        Toast.makeText(context, "Cannot get location.", Toast.LENGTH_SHORT).show()
                        Log.d("Store", "No Location Found")
                        progress.hide()
                    }
                    else {
                        try {
                            lifecycleScope.async{
                                var geocoder : Geocoder
                                var addresses: MutableList<Address>
                                geocoder = Geocoder(requireContext(), Locale.getDefault())
                                val lat = location.latitude
                                val lon = location.longitude
                                addresses = geocoder.getFromLocation(lat,lon,1) as MutableList<Address>

                                var city = addresses.get(0).locality
                                var state = addresses.get(0).adminArea
                                var postalCode = addresses.get(0).postalCode
                                var address = addresses.get(0).getAddressLine(0)
//
//                                Log.d("Store", "Address: $address")
//                                Log.d("Store", "City: $city")
//                                Log.d("Store", "State: $state")
//                                Log.d("Store", "Postal Code: $postalCode")

                               binding.addressEdittextField.setText("$address")
                                binding.stateEdittextField.setText("$state")
                                binding.postalCodeEdittextField.setText("$postalCode")
                                progress.hide()
                            }
                        }
                        catch (Ex: Exception){
                            progress.hide()
                            Toast.makeText(context,"Encounter error getting address",Toast.LENGTH_SHORT).show()
                            Log.d("Store","$Ex")
                        }
                    }
                }
                .addOnFailureListener{
                    Log.d("Store",it.toString())
                }
        }

        imageView = binding.updateStoreDetailsDetailsImg
        binding.updateStoreDetailsDetailsImg.setOnClickListener{
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }

        getStartTime(binding.operatingTimeEdittextField,requireContext())

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            imageView.setImageURI(imageUri)
            Log.d("Store",imageUri.toString())
            dataToFirebase = data
        }
    }

    private fun loadStoreDetailsDetails(){
        var uid = auth.currentUser?.uid.toString()

        database = FirebaseDatabase.getInstance().getReference("Stores")
        val dataRef = database.child(uid)

        val postListener = object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var storeImg = snapshot.child("storeImage").value.toString()
                var storeName = snapshot.child("storeName").value.toString()
                var storeDesc = snapshot.child("description").value.toString()
                var startTime = snapshot.child("startTime").value.toString()
                var endTime = snapshot.child("endTime").value.toString()
                var operatingDay = snapshot.child("operatingDay").value.toString()
                var email = snapshot.child("email").value.toString()
                var phone = snapshot.child("phone").value.toString()
                var address = snapshot.child("address").value.toString()
                var state = snapshot.child("state").value.toString()
                var postal = snapshot.child("postal").value.toString()
                var currentStoreID = snapshot.child("storeID").value.toString()

                oldStoreName = storeName
                oldImagePath = storeImg
                oldDesc = storeDesc
                oldOperatingDays = operatingDay
                oldEmail = email
                oldPhone = phone
                oldaddress = address
                oldState = state
                oldpostal = postal
                storeID = currentStoreID

                Log.d("Update", "This are Operating Days: $operatingDay")
                Picasso.get()
                    .load(storeImg)
                    .into(binding.updateStoreDetailsDetailsImg)

                binding.storeNameEdittextField.setText(storeName)
                binding.storeDescriptionEdittextField.setText(storeDesc)
                binding.operatingTimeEdittextField.setText(startTime + " - " + endTime)

                startingTime = startTime
                endingTime = endTime


                if(operatingDay.contains("Sunday,", ignoreCase = true)){
                    binding.sundayCheckBox.setChecked(true)
                }
                if(operatingDay.contains("Monday,", ignoreCase = true)){
//                    Log.d("Update","Monday is True")
                    binding.mondayCheckBox.setChecked(true)
                }
                if(operatingDay.contains("Tuesday,", ignoreCase = true)){
//                    Log.d("Update","Tuesday is True")
                    binding.tuesdayCheckBox.setChecked(true)
                }
                if(operatingDay.contains("Wednesday,")){
                    binding.wednesdayCheckBox.setChecked(true)
                }
                if(operatingDay.contains("Thursday,")){
                    binding.thursdayCheckBox.setChecked(true)
                }
                if(operatingDay.contains("Friday,")){
                    binding.fridayCheckBox.setChecked(true)
                }
                if(operatingDay.contains("Satuday,")){
                    binding.saturdayCheckBox.setChecked(true)
                }

                binding.storeEmailEdittextField.setText(email)
                binding.storePhoneEdittextField.setText(phone)

                binding.addressEdittextField.setText(address)
                binding.stateEdittextField.setText(state,false)
                binding.postalCodeEdittextField.setText(postal)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        dataRef.addValueEventListener(postListener)
    }

    private fun validationCheck(){

        var storeName = binding.storeNameTextField.editText?.text.toString()
        var description = binding.storeDescriptionTextField.editText?.text.toString()
//        var timeStart = startingTime
//        var timeEnd =   endingTime
        var operatingDays = "Sunday,Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,"
        var email = binding.storeEmailTextField.editText?.text.toString()
        var phone = binding.storePhoneTextField.editText?.text.toString()
        var errorChecker = false
        var storeNameExist = false

        if(oldStoreName == storeName){
            storeNameExist = false
        }
        else{
            for(data in allStoreNames){
                if(data == storeName){
                    storeNameExist = true
                }
            }
        }



//        if(imageUri == null){
//            binding.updateStoreDetailsDetailsImageTxt.text = "Must Upload Store Image"
//            binding.updateStoreDetailsDetailsImageTxt.setTextColor(resources.getColor(R.color.errorTextColor))
//            errorChecker = true
//        }
//        else{
//            binding.updateStoreDetailsDetailsImageTxt.visibility = View.INVISIBLE
//        }

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



        var checkStartingTime = SimpleDateFormat("HH:mm").parse(startingTime)
        var checkEndingTime = SimpleDateFormat("HH:mm").parse(endingTime)

//        var moreThenAHour = Math.abs(checkEndingTime.time - checkStartingTime.time)
        var moreThenAHour = checkEndingTime.time - checkStartingTime.time
        var checkHour = moreThenAHour / (1000*60*60) % 24
//        Log.d("Update", checkHour.toString())

        if(startingTime.isEmpty() || endingTime.isEmpty()){
            binding.operatingTimeTextField.error = "Invalid Operating Time"
            errorChecker = true
        }
        else if(endingTime < startingTime){
            binding.operatingTimeTextField.error = "Please Choose a Valid Operating Time"
            errorChecker = true
        }
        else if(checkHour < 1 ){
            binding.operatingTimeTextField.error = "Operating Time Must be 1 hour apart!"
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

//        Log.d("Store",operatingDays)

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
            Toast.makeText(context,"Please Check All Above Input Boxes!", Toast.LENGTH_SHORT).show()
        }else{
            if(dataToFirebase == null){
                updateStoreDetailsDetailsOldImage(oldImagePath!!,storeName,description, startingTime,
                    endingTime,operatingDays,email,phone)
//                Toast.makeText(context,"Store Has Been Updated!!", Toast.LENGTH_SHORT).show()
            }
            else{
                uploadToFirebase(dataToFirebase,storeName,description, startingTime,
                    endingTime,operatingDays,email,phone)
            }
        }
    }

    private fun updateStoreDetailsDetailsOldImage(storeImage:String,
                                                  storeName:String,
                                                  description:String,
                                                  startTime:String,
                                                  endTime:String,
                                                  operatingDay:String,
                                                  email:String,
                                                  phone:String){
        var uid = auth.currentUser?.uid.toString()
        database = FirebaseDatabase.getInstance().getReference("Stores")
        val dataRef = database.child(uid)

        var updateStoreDetailsDetails = CreateStoreData(
        storeID,storeName,description, startTime,endTime,operatingDay,email,phone, oldaddress,
            oldState, oldpostal,storeImage
        )

        dataRef.setValue(updateStoreDetailsDetails).addOnCompleteListener{
            if(it.isSuccessful){
                Toast.makeText(context,"Store Details Has Been Updated!" , Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(context,"Store Details Failed To Update" , Toast.LENGTH_SHORT).show()
            }
        }
    }

    private  fun  uploadToFirebase (data: Intent?,storeName:String,
                                    description:String,
                                    startTime:String,
                                    endTime:String,
                                    operatingDay:String,
                                    email:String,
                                    phone:String){
        val progress = ProgressDialog(activity)
        progress.setTitle("Uploading Image")
        progress.show()
        if(data != null){
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val refStorage = FirebaseStorage.getInstance().reference.child("images/$fileName")
            data?.data?.let {
                refStorage.putFile(it).addOnSuccessListener(
                    OnSuccessListener<UploadTask.TaskSnapshot>{
                            taskSnapshot -> taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                        imagePathFromFirebase = it.toString()
                        updateStoreDetailsDetailsOldImage(imagePathFromFirebase!!,storeName,
                        description,startTime,endTime,operatingDay,email,phone)
                        progress.hide()
                    }
                    })
            }
        }
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

    private fun getStartTime(textView: TextView, context: Context){

        val cal = Calendar.getInstance()

        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)

//            checkStartingTime = cal.time

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

//            checkEndingTime = cal.time

            endingTime = SimpleDateFormat("HH:mm").format(cal.time)
            textView.append(" - " + SimpleDateFormat("HH:mm").format(cal.time))
        }

        TimePickerDialog(context, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    private fun addressValidation(){
        var address = binding.addressTextField.editText?.text.toString()
        var state = binding.stateTextField.editText?.text.toString()
        var postal = binding.postalCodeTextField.editText?.text.toString()
        var errorChecker = false

        if(address.isEmpty()){
            binding.addressTextField.error = "Required*"
            errorChecker = true
        }
        else if(address.count() > 150){
            binding.addressTextField.error = "Cannot be more then 150"
            errorChecker = true
        }
        else{
            binding.addressTextField.isErrorEnabled = false
        }

        if(postal.isEmpty()){
            binding.postalCodeTextField.error = "Required*"
            errorChecker = true
        }
        else if(postal.count() > 5){
            binding.postalCodeTextField.error = "Cannot be more then 5"
            errorChecker = true
        }
        else{
            binding.postalCodeTextField.isErrorEnabled = false
        }

        if(errorChecker){
            Toast.makeText(context,"Check Input Boxes",Toast.LENGTH_SHORT).show()
        }
        else{
            updateAddress(address,state,postal)
        }
    }

    private fun updateAddress(address:String,state:String,postal:String){
        var uid = auth.currentUser?.uid.toString()
        database = FirebaseDatabase.getInstance().getReference("Stores")
        val dataRef = database.child(uid)

        var updateStoreDetailsDetails = CreateStoreData(
            storeID, oldStoreName, oldDesc, startingTime,
            endingTime, oldOperatingDays, oldEmail, oldPhone, address,
            state, postal, oldImagePath
        )

        dataRef.setValue(updateStoreDetailsDetails).addOnCompleteListener{
            if(it.isSuccessful){
                Toast.makeText(context,"Store Address Has Been Updated!" , Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(context,"Store Address Failed To Update" , Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            MY_PERMISSIONS_REQUEST_LOCATION
        )
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
//        private const val MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 66
    }
}