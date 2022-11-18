package com.example.stationerygo.StoreCreate

data class CreateStoreData(
    val storeID: String ?= null,
    val storeName:String ?= null,
    val description:String ?= null,
    val startTime:String ?= null,
    val endTime:String ?= null,
    val operatingDay:String ?= null,
    val email:String ?= null,
    val phone:String ?= null,
    val address:String ?= null,
    val lat:String ?= null,
    val lon:String ?= null,
    val storeImage: String ?= null,
)
