package com.example.stationerygo.StorePage.StoreDetailPage

data class StoreProductData(
    val productPosition : Int ?= null,
    val productID: String ?= null,
    val productImage :String ?= null,
    val productName : String ?= null,
    val productQty : String ?= null,
    val productPrice: String ?= null,
)
