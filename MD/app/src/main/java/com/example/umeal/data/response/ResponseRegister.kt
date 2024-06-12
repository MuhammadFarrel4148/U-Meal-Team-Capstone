package com.example.umeal.data.response

import com.google.gson.annotations.SerializedName

data class ResponseRegister(
    @field:SerializedName("error")
    val status: Boolean,

    @field:SerializedName("message")
    val message: String,
)