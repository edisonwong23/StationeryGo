package com.example.stationerygo.OrderList

data class OrderListData(
    val orderID: String ?= null,
    val orderShop: String ?= null,
    val orderDate: String ?= null,
    val orderStatus: String ?= null,
    val orderImage: String ?= null,
)
