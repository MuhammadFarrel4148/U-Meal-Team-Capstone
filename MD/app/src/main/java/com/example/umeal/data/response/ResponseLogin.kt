package com.example.umeal.data.response

import com.google.gson.annotations.SerializedName
data class ResponseLogin(
    @field:SerializedName("error")
    val status: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("token")
    val token: String
)
