package com.example.stationerygo.StoreOwner.OrderPage

data class ShopOrderData (
    val orderID: String ?= null,
    val orderDate: String ?= null,
    val orderStatus: String ?= null,
    val userImage: String ?= null,
    val userName: String ?= null,
)