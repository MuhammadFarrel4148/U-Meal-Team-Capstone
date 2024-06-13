package com.example.umeal.home.ui.scan

import com.google.gson.annotations.SerializedName

data class ResponseScanImage (
    @field:SerializedName("error")
    val status: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("data")
    val data: ScanData,

)
data class ScanData (
    @field:SerializedName("id")
    val id: String,

    @field:SerializedName("result")
    val result: List<String>,

    @field:SerializedName("createdAt")
    val createdAt: String,
)