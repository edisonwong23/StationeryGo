package com.example.stationerygo.Cart

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stationerygo.R
import com.example.stationerygo.databinding.FragmentCartPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.paypal.checkout.PayPalCheckout
import com.paypal.checkout.approve.OnApprove
import com.paypal.checkout.cancel.OnCancel
import com.paypal.checkout.config.CheckoutConfig
import com.paypal.checkout.config.Environment
import com.paypal.checkout.config.SettingsConfig
import com.paypal.checkout.createorder.*
import com.paypal.checkout.error.OnError
import com.paypal.checkout.order.Amount
import com.paypal.checkout.order.AppContext
import com.paypal.checkout.order.Order
import com.paypal.checkout.order.PurchaseUnit

private lateinit var binding : FragmentCartPageBinding
private lateinit var database: DatabaseReference
private lateinit var auth: FirebaseAuth
private var totalPaymentAmount: String = ""
private var itemInCart = false
private var deliveryFee = "0.00"
private var orderType = "Pick Up"
private var userCurrentAddress = "None"
private var isDelivery = false
private var storeName = ""
private var storeAddress = ""
private var totalPalpayAmount = ""
private val YOUR_CLIENT_ID = "AS2mc_jiy0tnmtlt-fIHxoznNbsWL0K8rG7kjLo4vJUdH28VV1LUj5zYHeiu-kDkfArlY_nYTTucDqAZ"


class Cart_Page : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
             binding = FragmentCartPageBinding.inflate(
            inflater,
            container,
            false
        )
        auth = FirebaseAuth.getInstance()
        getShopName()
        loadRecyclerCart()
        getUserAddress()

        binding.pickupBtn.setOnClickListener {
            binding.pickupBtn.backgroundTintList = resources.getColorStateList(R.color.choosenButton)
            binding.deliveryBtn.backgroundTintList = resources.getColorStateList(R.color.notChoosenButton)
            deliveryFee = "0.00"
            orderType = "Pick Up"
            binding.deliveryFeeAmountTxt.text = deliveryFee
            binding.addressEditCard.visibility = View.GONE
            isDelivery = false
            loadRecyclerCart()
        }

        binding.deliveryBtn.setOnClickListener {
            binding.pickupBtn.backgroundTintList = resources.getColorStateList(R.color.notChoosenButton)
            binding.deliveryBtn.backgroundTintList = resources.getColorStateList(R.color.choosenButton)
            deliveryFee = "5.00"
            orderType = "Delivery"
            binding.deliveryFeeAmountTxt.text = deliveryFee
            binding.addressEditCard.visibility = View.VISIBLE
            isDelivery = true
            loadRecyclerCart()
        }

        binding.clearCartCartBtn.setOnClickListener {
            val alartDialog = AlertDialog.Builder(context,R.style.AlertDialogCustom)
            alartDialog.apply {
                setTitle("Clear Cart?")
                setMessage("Are you sure you want to clear cart?")
                setPositiveButton("Clear Cart"){ _: DialogInterface?, _: Int ->
                    clearCart()
                }
                setNegativeButton("Cancel"){_, _ ->
                }
            }.create().show()
        }

//        binding.proceedPaymentBtn.setOnClickListener{
//
//            if(itemInCart){
//                proceedToPayment()
//            }
//           else
//               Toast.makeText(context,"Cart Is Empty,Please Add Item to Cart!",Toast.LENGTH_SHORT).show()
//        }

        val config = CheckoutConfig(
            application = requireActivity().application,
            clientId = YOUR_CLIENT_ID,
            environment = Environment.SANDBOX,
            returnUrl = "com.example.stationerygo://paypalpay",
            currencyCode = CurrencyCode.MYR,
            userAction = UserAction.PAY_NOW,
            settingsConfig = SettingsConfig(
                loggingEnabled = true
            )
        )
        PayPalCheckout.setConfig(config)

        return binding.root
    }

    private fun paypalButton(){

        binding.paymentButtonContainer.setup(
            createOrder =
            CreateOrder { createOrderActions ->

                val order =
                    Order(
                        intent = OrderIntent.CAPTURE,
                        appContext = AppContext(userAction = UserAction.PAY_NOW,
                            shippingPreference = ShippingPreference.NO_SHIPPING
                        ),
                        purchaseUnitList =
                        listOf(
                            PurchaseUnit(
                                amount =
                                Amount(currencyCode = CurrencyCode.MYR, value = totalPaymentAmount),
                            )
                        )
                    )
                createOrderActions.create(order)
            },
            onApprove =
            OnApprove{ approval ->
                approval.orderActions.capture{ captureOrderResult ->
                    proceedToPayment()
                }
            },
            onCancel = OnCancel {
                Toast.makeText(context,"Paypal has been canceled",Toast.LENGTH_SHORT).show()
            },
            onError = OnError { errorInfo ->
                Log.d("Payment", errorInfo.toString())
                Toast.makeText(context,"Error Connecting Paypal",Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun getShopName(){
            var getStoreID = arguments?.getString("StoreID").toString()

            database = FirebaseDatabase.getInstance().getReference("Stores")

        val postListener = object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    if(it.child("storeID").value!!.equals(getStoreID)){
                        storeName = it.child("storeName").value.toString()
                        storeAddress = it.child("address").value.toString()
                    }
                    else{
                        Log.d("Cart","Rejected ")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        database.addListenerForSingleValueEvent(postListener)
    }

    private fun clearCart(){
            var uid = auth.currentUser?.uid.toString()
            database = FirebaseDatabase.getInstance().getReference("Cart")
            var clearCartRef = database.child(uid)

            clearCartRef.removeValue().addOnCompleteListener {
                if(it.isSuccessful){
                    Toast.makeText(context,"Cart Has Been Cleared",Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                else
                    Toast.makeText(context,"Error Clearing Cart",Toast.LENGTH_SHORT).show()
            }
    }

    private fun getUserAddress(){
        var uid = auth.currentUser?.uid.toString()
        database = FirebaseDatabase.getInstance().getReference("Users")
        val userRef = database.child(uid)

        val postListner = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var userAddress = snapshot.child("address").value.toString()
                var userNo = snapshot.child("livingNo").value.toString()
                var userType = snapshot.child("livingType").value.toString()

                binding.cartCurrentAddressTxt.text = userAddress
                userCurrentAddress = userAddress
                binding.cartLivingNoTxt.text = userNo
                binding.cartLivingTypeTxt.text = userType
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        userRef.addListenerForSingleValueEvent(postListner)
    }

    private fun loadRecyclerCart(){
        var uid = auth.currentUser?.uid.toString()
        var storeID = arguments?.getString("StoreID").toString()
        var cartArray = ArrayList<Cart_Data>()

        database = FirebaseDatabase.getInstance().getReference("Cart")
        var dataRef = database.child(uid).child(storeID)
        val postListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                itemInCart = false
                cartArray.clear()
                var productQtyTotal = 0
                var productPriceTotal = 0.00
                snapshot.children.forEach{
                    var productID = it.child("itemID").value.toString()
                    var productImage = it.child("itemImage").value.toString()
                    var productName = it.child("itemName").value.toString()
                    var productQty = it.child("itemQty").value.toString()
                    var productPrice = it.child("itemPrice").value.toString()
                    var itemCurrentAmount = it.child("itemCurrentAmount").value.toString()
                    var currentPrice = productPrice.toDouble() * productQty.toInt()

                    productQtyTotal += productQty.toInt()

                    productPriceTotal += currentPrice
                    itemInCart = true
                    cartArray.add(Cart_Data(productID,productImage,productName,productQty,productPrice,itemCurrentAmount,storeID,uid))
                }
//                  Log.d("Cart", "Total Qty: $productQtyTotal")
//                  Log.d("Cart", "Total Price: $productPriceTotal")

                  var subTotal = productPriceTotal
                  binding.subTotalAmountTxt.text = "%.2f".format(subTotal)

                 binding.deliveryFeeAmountTxt.text = deliveryFee

                 var tax = productPriceTotal * 0.06
                 binding.taxAmountTxt.text = "%.2f".format(tax)

                 var totalAmount = subTotal+ deliveryFee.toDouble() + tax
                 binding.totalAmountAmountTxt.text = "%.2f".format(totalAmount)
                 totalPaymentAmount = "%.2f".format(totalAmount)
//                Log.d("Cart",cartArray.toString())

                if(itemInCart){
                    paypalButton()
                    binding.paymentButtonContainer.visibility = View.VISIBLE
                }
                else{
                    binding.paymentButtonContainer.visibility = View.GONE
                }

                val recycleView = binding.cartRecyclerview
                recycleView.layoutManager = LinearLayoutManager(context)

                recycleView.adapter = CartAdapet(cartArray){Cart_Data,position:Int ->

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Cart",error.toString())
            }

        }
        dataRef.addValueEventListener(postListener)
    }

    private fun proceedToPayment(){

        if(isDelivery == false){
            userCurrentAddress = "None"
        }

        Log.d("Cart", storeName)

        var storeID = arguments?.getString("StoreID").toString()
        var bundle = bundleOf(
            "totalAmount" to totalPaymentAmount,
            "StoreName" to storeName,
            "orderType" to orderType,
            "storeID" to storeID,
            "userCurrentAddress" to userCurrentAddress,
            "storeAddress" to storeAddress
        )
        findNavController().navigate(R.id.action_cart_Page_to_paymentPage,bundle)
    }
}
