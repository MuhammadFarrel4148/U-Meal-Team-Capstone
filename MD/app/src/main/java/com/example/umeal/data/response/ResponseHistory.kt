package com.example.umeal.data.response

import com.google.gson.annotations.SerializedName

data class ResponseHistory(

    @field:SerializedName("data")
    val data: List<DataItem>? = null,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("status")
    val status: String
)

data class DataItem(

    @field:SerializedName("detectedFoods")
    val detectedFoods: List<DetectedFoodsItem?>,

    @field:SerializedName("image_url")
    val imageUrl: String,

    @field:SerializedName("total_kalori")
    val totalKalori: Int,

    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("scan_timestamp")
    val scanTimestamp: String
)

