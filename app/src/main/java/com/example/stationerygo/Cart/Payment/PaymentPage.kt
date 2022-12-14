package com.example.stationerygo.Cart.Payment

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.stationerygo.Cart.Cart_Data
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentPaymentPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.paypal.checkout.PayPalCheckout
import com.paypal.checkout.approve.OnApprove
import com.paypal.checkout.cancel.OnCancel
import com.paypal.checkout.config.CheckoutConfig
import com.paypal.checkout.config.Environment
import com.paypal.checkout.config.SettingsConfig
import com.paypal.checkout.createorder.*
import com.paypal.checkout.error.OnError
import com.paypal.checkout.order.*
import com.paypal.pyplcheckout.addshipping.model.Country
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

private lateinit var binding : FragmentPaymentPageBinding
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth
private var selectedPaymentType: String = ""
private var cartData = ArrayList<Cart_Data>()
private var minusProductID : MutableList<String> = ArrayList<String>()
private var minusProductQty :MutableList<String> = ArrayList<String>()
private var userLat = ""
private var userLon = ""
private var livingNo = ""
private var livingType = ""
private var oldOrderID = ""
private val YOUR_CLIENT_ID = "AS2mc_jiy0tnmtlt-fIHxoznNbsWL0K8rG7kjLo4vJUdH28VV1LUj5zYHeiu-kDkfArlY_nYTTucDqAZ"

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

        val stationeryBind = binding.expireMonthEdittextField
        var stationery = resources.getStringArray(R.array.Month)
        stationery = stationery
        val adapter = ArrayAdapter(requireContext(),android.R.layout.simple_list_item_1,stationery)
        stationeryBind.setAdapter(adapter)

        auth = FirebaseAuth.getInstance()

        getUserLatLon()
        loadData()

        spinnerAdapter()

        loadCartData()





        binding.confirmOrderBtn.setOnClickListener{
            validatePayment()
        }

        binding.returnToMainPageBtn4.setOnClickListener {
            findNavController().navigate(R.id.action_paymentPage_to_homePage)
        }

        binding.paymentCancelOrderBtn.setOnClickListener {
            val alartDialog = AlertDialog.Builder(context,R.style.AlertDialogCustom)
            alartDialog.apply {
                setTitle("Confirm Cancel Order?")
                setMessage("Are you sure you want to cancel order? Refunding Order might take a while")
                setPositiveButton("Confirm"){ _: DialogInterface?, _: Int ->
                    orderCancel()
                }
                setNegativeButton("Cancel"){_, _ ->
                }
            }.create().show()
        }

        return binding.root
    }

    private fun getUserLatLon(){
        var uid = auth.currentUser?.uid.toString()
        database = FirebaseDatabase.getInstance().getReference("Users")

        var readRef = database.child(uid)

        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var lat = snapshot.child("lat").value.toString()
                var lon = snapshot.child("lon").value.toString()
                var no = snapshot.child("livingNo").value.toString()
                var type = snapshot.child("livingType").value.toString()

                userLat = lat
                userLon = lon
                livingNo = no
                livingType = type
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        readRef.addListenerForSingleValueEvent(postListener)
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
        var cartMinusArray = ArrayList<Cart_Data>()

        database = FirebaseDatabase.getInstance().getReference("Cart")
        var dataRef = database.child(uid).child(storeID)

        val postListenr = object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                cartArray.clear()
                minusProductID.clear()
                minusProductQty.clear()
                cartData.clear()
                snapshot.children.forEach{
                    var productID = it.child("itemID").value.toString()
                    var productImage = it.child("itemImage").value.toString()
                    var productName = it.child("itemName").value.toString()
                    var productQty = it.child("itemQty").value.toString()
                    var productPrice = it.child("itemPrice").value.toString()
                    var currentAmount = it.child("itemCurrentAmount").value.toString()
                    var newCurrentQty = currentAmount.toInt() - productQty.toInt()

                    minusProductID.add(productID)
                    minusProductQty.add(newCurrentQty.toString())
                    cartArray.add(Cart_Data(productID,productImage,productName,productQty,productPrice))
                }
                cartData = cartArray
//                Log.d("Payment", "Current Data:" + cartData.toString())
                confirmOrder()
                progress.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Payment","Encounter Error $error")
                progress.dismiss()
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

                if(paymentType == "Cash"){
                    binding.paymentDetailsCard.setCardBackgroundColor(resources.getColor(androidx.cardview.R.color.cardview_shadow_start_color))
                    var layout = binding.paymentDetailsConstraintLayout
                    var i = 0
                    var layoutCount = layout.childCount
//                    Log.d("Payment", layoutCount.toString())
                    for(data in 1..layoutCount){
                        var child = layout.getChildAt(i)
                        child.setEnabled(false)
                        i++
                    }
//                    binding.paymentButtonContainer.visibility = View.GONE
//                    binding.confirmOrderBtn.visibility = View.VISIBLE
                }
                else{
//                    binding.paymentButtonContainer.visibility = View.VISIBLE
//                    binding.confirmOrderBtn.visibility = View.GONE

                    binding.paymentDetailsCard.setCardBackgroundColor(resources.getColor(androidx.cardview.R.color.cardview_light_background))
                    var layout = binding.paymentDetailsConstraintLayout
                    var i = 0
                    var layoutCount = layout.childCount
                    Log.d("Payment", layoutCount.toString())
                    for(data in 1..layoutCount){
                        var child = layout.getChildAt(i)
                        child.setEnabled(true)
                        i++
                    }
                }
//                Log.d("Payment","Selected Payment is $selectedPaymentType")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun validatePayment(){

        val ptVisa = "^4[0-9]{6,}$"
        var pattern_VISA = Pattern.compile(ptVisa)

        val ptMasterCard = "^5[1-5][0-9]{5,}$"
        var pattern_MASTER = Pattern.compile(ptMasterCard)

        var cardNumber = binding.cardNumberTextField.editText?.text.toString()
        var expMonth = binding.expireMonthTextField.editText?.text.toString()
        var expYear = binding.expireYearTextField.editText?.text.toString()
        var cvv = binding.cvcCodeTextField.editText?.text.toString()
        var errorChecker = false

        if(cardNumber.isEmpty()){
            binding.cardNumberTextField.error = "Invalid Card Number"
            errorChecker = true
        }
        else{
            binding.cardNumberTextField.isErrorEnabled = false
        }

        if(selectedPaymentType == "Visa")
        {
            if(!pattern_VISA.matcher(cardNumber).matches())
            {
                binding.cardNumberTextField.error = "Invalid VISA Number"
                errorChecker = true
            }
            else{
                binding.cardNumberTextField.isErrorEnabled = false
            }
        }
        else if(selectedPaymentType == "MasterCard")
        {
            if(!pattern_MASTER.matcher(cardNumber).matches())
            {
                binding.cardNumberTextField.error = "Invalid Master Card Number"
                errorChecker = true
            }
            else{
                binding.cardNumberTextField.isErrorEnabled = false
            }
        }

        if(expMonth.isEmpty()){
            binding.expireMonthTextField.error = "Required*"
            errorChecker = true
        }
        else{
            binding.expireMonthTextField.isErrorEnabled = false
        }

        if(expYear.isEmpty()){
            binding.expireYearTextField.error = "Required*"
            errorChecker = true
        }
        else if(expYear.count() > 4){
            binding.expireYearTextField.error = "Invalid Year*"
            errorChecker = true
        }
        else{
            binding.expireYearTextField.isErrorEnabled = false
        }

        if(cvv.isEmpty()){
            binding.cvcCodeTextField.error = "Invalid Year*"
            errorChecker = true
        }
        else if(cvv.count() > 3){
            binding.cvcCodeTextField.error = "Invalid CVC*"
            errorChecker = true
        }
        else{
            binding.cvcCodeTextField.isErrorEnabled = false
        }

        if(selectedPaymentType == "Cash"){
            confirmOrder()
        }
        else if(selectedPaymentType == "Paypal"){
            checkPayPal()
        }
        else{
            Toast.makeText(context,"Please Check Above Input Boxes!",Toast.LENGTH_SHORT).show()
        }

    }

    private fun orderCancel(){
        database = FirebaseDatabase.getInstance().getReference("Orders")

        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    var checkOrderID = it.child("orderID").value.toString()
                    if(checkOrderID == oldOrderID){
                        var orderKey = it.key.toString()
                        database.child(orderKey).removeValue().addOnSuccessListener {
                            Toast.makeText(context,"Order has been Cancel",Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_paymentPage_to_homePage)
                        }.addOnFailureListener {
                            Toast.makeText(context,"Order Failed to Delete",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        }

        database.addValueEventListener(postListener)
    }

    private fun checkPayPal(){

    }

    private fun confirmOrder(){
        val progress = ProgressDialog(activity)
        progress.show()
        var uid = auth.currentUser?.uid.toString()
        var totalAmount = arguments?.getString("totalAmount").toString()
        var storeName = arguments?.getString("StoreName").toString()
        var orderType = arguments?.getString("orderType").toString()
        var storeID = arguments?.getString("storeID").toString()
        var paymentType = "Paypal"
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val currentDate = sdf.format(Date())
        val currentStatus = "Pending"
        val userCurrentAddress = arguments?.getString("userCurrentAddress").toString()

        binding.paymentpaymentAmountTxt3.text = "RM "+totalAmount
        binding.paymentDateTxt3.text = currentDate
//        Log.d("Payment",currentDate.toString())

        if(userCurrentAddress == "None"){
            userLat = "None"
            userLon = "None"
        }

        database = FirebaseDatabase.getInstance().getReference("Orders")
        var orderID = UUID.randomUUID().toString()
        oldOrderID = orderID
        var dataRef = database.push().
        setValue(PaymentData(orderID,
            storeID,
            uid,
            paymentType,
            orderType,
            totalAmount,
            currentDate,
            cartData,
            currentStatus,
            userCurrentAddress,
            userLat,
            userLon,
        livingNo,
        livingType))
            .addOnCompleteListener{
                progress.dismiss()
            Toast.makeText(context,"Purchase Complete",Toast.LENGTH_SHORT).show()
                decreaseStock()
        }.addOnFailureListener {
                progress.dismiss()
            Toast.makeText(context,"Fail To Purchase",Toast.LENGTH_SHORT).show()
            Log.d("Payment",it.toString())
        }
    }

    private fun decreaseStock(){
        var storeID = arguments?.getString("storeID").toString()
        database = FirebaseDatabase.getInstance().getReference("Products")
        var storeRef = database.child(storeID)

        var i = 0
//        Log.d("Payment", minusProductID.toString())
        for (data in minusProductID){
            storeRef.child(minusProductID[i]).child("productQty").setValue(minusProductQty[i]).addOnCompleteListener{
                if(it.isSuccessful){
//                    Log.d("Payment", minusProductID[i])
                }
                else
                    Toast.makeText(context,"Fail To Purchase",Toast.LENGTH_SHORT).show()
            }
            i++
        }
        clearCart()
    }

    private fun clearCart(){
        var uid = auth.currentUser?.uid.toString()
        database = FirebaseDatabase.getInstance().getReference("Cart")

        database.child(uid).removeValue().addOnCompleteListener{
            if(it.isSuccessful){
//                Toast.makeText(context,"Payment Done",Toast.LENGTH_SHORT).show()
            }
            else{
//                Toast.makeText(context,"Fail To Purchase",Toast.LENGTH_SHORT).show()
            }
        }
    }


}