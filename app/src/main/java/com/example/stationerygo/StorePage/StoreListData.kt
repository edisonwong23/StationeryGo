package com.example.stationerygo.StorePage

data class StoreListData(
    val position: Int ?= null,
    val storeImage: String ?= null,
    val storeName: String ?= null,
    val city: String ?= null,
    val startTime: String ?= null,
    val endTime: String ?= null,
    val status: String ?= null,
)
