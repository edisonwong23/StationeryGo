package com.example.stationerygo.Cart.Payment

import com.example.stationerygo.Cart.Cart_Data

data class PaymentData(
    val orderID: String ?= null,
    val storeID: String ?= null,
    val userID: String ?= null,
    val paymentType: String ?= null,
    val orderType: String ?= null,
    val totalAmount: String ?= null,
    val purchaseDate: String ?= null,
    val itemOrdered: ArrayList<Cart_Data>,
    val orderStatus: String ?= null,
    val address: String ?= null,
    val lat: String ?= null,
    val lon: String ?= null,
    val userLivingNo : String ?= null,
    val userLivingType: String ?= null,
)
