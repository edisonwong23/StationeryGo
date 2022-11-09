package com.example.stationerygo.Cart.Payment

import com.example.stationerygo.Cart.Cart_Data

data class PaymentData(
    val storeID: String ?= null,
    val userID: String ?= null,
    val paymentType: String ?= null,
    val orderType: String ?= null,
    val totalAmount: String ?= null,
    val purchaseDate: String ?= null,
    val itemOrdered: ArrayList<Cart_Data>,
)
