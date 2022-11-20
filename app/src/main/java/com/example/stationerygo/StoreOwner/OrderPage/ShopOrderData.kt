package com.example.stationerygo.StoreOwner.OrderPage

data class ShopOrderData (
    val orderKey: String ?= null,
    val orderID: String ?= null,
    val orderDate: String ?= null,
    val orderStatus: String ?= null,
    val userImage: String ?= null,
    val userName: String ?= null,
    val userID: String ?= null,
    val orderType: String ?= null,
)