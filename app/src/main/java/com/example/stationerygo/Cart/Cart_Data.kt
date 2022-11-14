package com.example.stationerygo.Cart

data class Cart_Data(

    val itemID : String ?= null,
    val itemImage : String ?= null,
    val itemName : String ?= null,
    val itemQty : String ?= null,
    val itemPrice : String ?= null,
    val itemCurrentAmount: String ?= null,
    val shopID: String ?= null,
    val userID: String ?= null,
)
