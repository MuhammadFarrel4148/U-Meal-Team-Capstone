package com.example.umeal.data.response

import com.google.gson.annotations.SerializedName

data class ForgotPasswordResponse(

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("status")
    val status: String
)
