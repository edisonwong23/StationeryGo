package com.example.stationerygo.StoreCreate

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.stationerygo.LoginPage.imagePathFromFirebase
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentCreateStoreAddressPageBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.async
import java.lang.Exception
import java.util.*

private lateinit var binding : FragmentCreateStoreAddressPageBinding
private lateinit var fusedLocationClient: FusedLocationProviderClient
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth

class CreateStoreAddressPage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateStoreAddressPageBinding.inflate(
            inflater,
            container,
            false
        )


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
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

                                Log.d("Store", "Address: $address")
                                Log.d("Store", "City: $city")
                                Log.d("Store", "State: $state")
                                Log.d("Store", "Postal Code: $postalCode")

                                binding.addressEdittextField.setText("$address")
                                binding.stateEdittextField.setText("$state")
                                binding.cityEdittextField.setText("$city")
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

        binding.createStoreBtn.setOnClickListener{
            insertToFileStorage()
        }

        return binding.root
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

    private fun insertToFileStorage(){
        val data = arguments?.getString("storeImage")?.toUri()
        Log.d("Store",data.toString())
        if(data != null){
            val progress = ProgressDialog(activity)
            progress.setTitle("Uploading Image To Storage")
            progress.show()
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val refStorage = FirebaseStorage.getInstance().reference.child("store_images/$fileName")
            refStorage.putFile(data).addOnSuccessListener(
                OnSuccessListener<UploadTask.TaskSnapshot>{
                        taskSnapshot -> taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                    imagePathFromFirebase = it.toString()
                    progress.hide()
                    getCurrentUser(imagePathFromFirebase)
                }
                }).addOnFailureListener{
                    Log.d("Store",it.toString())
            }
        }
    }

    private fun getCurrentUser(imagePathFromFirebase:String){
        auth = Firebase.auth
        val currentUser = Firebase.auth.currentUser
        currentUser?.let {
            var getCurrentUser = currentUser.email.toString()
            var uid = currentUser.uid.toString()
            insetToDatabase(imagePathFromFirebase,getCurrentUser,uid)
        }
    }

    private fun insetToDatabase(imagePathFromFirebase:String,currentUser:String,uid:String){


        val progress = ProgressDialog(activity)
        progress.setTitle("Uploading Store Details")
        progress.show()

        database = FirebaseDatabase.getInstance().getReference("Stores")
        val storeName = arguments?.getString("storeName").toString()
        val description = arguments?.getString("description").toString()
        val timeStart = arguments?.getString("timeStart").toString()
        val timeEnd = arguments?.getString("timeEnd").toString()
        val dayStart = arguments?.getString("dayStart").toString()
        val dayEnd = arguments?.getString("dayEnd").toString()
        val email = arguments?.getString("email").toString()
        val phone = arguments?.getString("phone").toString()
        val address = binding.addressTextField.editText?.text.toString()
        val state = binding.stateTextField.editText?.text.toString()
        val postal = binding.postalCodeTextField.editText?.text.toString()
        val city = binding.cityTextField.editText?.text.toString()

        val user = CreateStoreData(currentUser,storeName,description,timeStart,timeEnd,dayStart,dayEnd,email,phone,address,state,postal,city,imagePathFromFirebase)

        database.child(uid).setValue(user).addOnCompleteListener {
            Toast.makeText(getContext(), "Store Created", Toast.LENGTH_SHORT).show()
            progress.hide()
            findNavController().navigate(R.id.action_createStoreAddressPage_to_mainStorePage)
        }.addOnFailureListener{
            Log.d("Store", "Failure happen" +it.toString())
            Toast.makeText(getContext(), it.toString(), Toast.LENGTH_SHORT).show()
            progress.hide()
        }
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
//        private const val MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 66
    }

}