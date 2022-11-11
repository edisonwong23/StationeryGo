package com.example.stationerygo.StoreProducts

data class ProductListData(
    val storeID: String ?= null,
    val productKey: String ?= null,
    val productImage:String ?=null,
    val productName:String ?= null,
    val productQty: String ?= null,
)
