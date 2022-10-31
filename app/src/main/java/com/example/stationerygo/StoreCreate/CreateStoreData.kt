package com.example.stationerygo.StoreCreate

data class CreateStoreData(
    val owner: String ?= null,
    val storeName:String ?= null,
    val description:String ?= null,
    val startTime:String ?= null,
    val endTime:String ?= null,
    val startDay:String ?= null,
    val endDay:String ?= null,
    val email:String ?= null,
    val phone:String ?= null,
    val address:String ?= null,
    val state:String ?= null,
    val postal:String ?= null,
    val city:String ?= null,
    val storeImage: String ?= null,
)
