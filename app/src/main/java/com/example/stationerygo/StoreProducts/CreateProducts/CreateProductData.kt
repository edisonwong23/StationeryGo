package com.example.stationerygo.StoreProducts.CreateProducts

data class CreateProductData(
    var productImage: String?=null,
    var productName: String?= null,
    var productDescription: String ?= null,
    var productType:String ?= null,
    var productQty: String ?= null,
    var productPrice: String ?= null,
)
