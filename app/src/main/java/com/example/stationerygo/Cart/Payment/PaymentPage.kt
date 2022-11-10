package com.example.stationerygo.Cart.Payment

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.stationerygo.Cart.Cart_Data
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentCartPageBinding
import com.example.stationerygo.databinding.FragmentPaymentPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.async
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private lateinit var binding : FragmentPaymentPageBinding
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth
private var selectedPaymentType: String = ""
private var cartData = ArrayList<Cart_Data>()

class PaymentPage : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentPaymentPageBinding.inflate(
            inflater,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()

        loadData()

        spinnerAdapter()

        loadCartData()

        binding.confirmOrderBtn.setOnClickListener{
            confirmOrder()
        }

        return binding.root
    }

    private fun loadData(){
        var totalAmount = arguments?.getString("totalAmount").toString()
        var storeName = arguments?.getString("StoreName").toString()
        var orderType = arguments?.getString("orderType").toString()

        binding.totalAmountRinggitTxt.text = totalAmount
        binding.storeNameTxt.text = storeName
        binding.orderTypeTxt.text = orderType
    }

    private fun loadCartData(){
        val progress = ProgressDialog(activity)
        progress.show()

        var uid = auth.currentUser?.uid.toString()
        var storeID = arguments?.getString("storeID").toString()
        var cartArray = ArrayList<Cart_Data>()

        database = FirebaseDatabase.getInstance().getReference("Cart")
        var dataRef = database.child(uid).child(storeID)

        val postListenr = object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                cartArray.clear()
                snapshot.children.forEach{
                    var productID = it.child("itemID").value.toString()
                    var productImage = it.child("itemImage").value.toString()
                    var productName = it.child("itemName").value.toString()
                    var productQty = it.child("itemQty").value.toString()
                    var productPrice = it.child("itemPrice").value.toString()

                    cartArray.add(Cart_Data(productID,productImage,productName,productQty,productPrice))
                }
                cartData = cartArray
//                Log.d("Payment", "Current Data:" + cartData.toString())
                progress.hide()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Payment","Encounter Error $error")
                progress.hide()
            }

        }
        dataRef.addListenerForSingleValueEvent(postListenr)
    }


    private fun spinnerAdapter(){
        val spinner: Spinner = binding.paymentTypeSpinner
        val payment = resources.getStringArray(R.array.Payments)
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.Payments,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                var paymentType = payment[p2]
                selectedPaymentType = paymentType
//                Log.d("Payment","Selected Payment is $selectedPaymentType")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun confirmOrder(){
        var uid = auth.currentUser?.uid.toString()
        var totalAmount = arguments?.getString("totalAmount").toString()
        var storeName = arguments?.getString("StoreName").toString()
        var orderType = arguments?.getString("orderType").toString()
        var storeID = arguments?.getString("storeID").toString()
        var paymentType = selectedPaymentType
        val sdf = SimpleDateFormat("dd/M/yyyy")
        val currentDate = sdf.format(Date())
        val currentStatus = "Pending"
//        Log.d("Payment",currentDate.toString())


        database = FirebaseDatabase.getInstance().getReference("Orders")
        var orderID = UUID.randomUUID().toString()
        var dataRef = database.push().
        setValue(PaymentData(orderID,
            storeID,
            uid,
            paymentType,
            orderType,
            totalAmount,
            currentDate,
            cartData,
            currentStatus))
            .addOnCompleteListener{
            Toast.makeText(context,"Purchase Complete",Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context,"Fail To Purchase",Toast.LENGTH_SHORT).show()
            Log.d("Payment",it.toString())
        }
    }

}